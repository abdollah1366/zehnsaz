package com.barati.zehnsaz.ui.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barati.zehnsaz.domain.engine.GameStatus
import com.barati.zehnsaz.domain.engine.InhibitoryControlEngine
import com.barati.zehnsaz.domain.model.*
import com.barati.zehnsaz.domain.progression.GameSessionManager
import com.barati.zehnsaz.domain.scoring.InhibitoryControlScoreCalculator
import com.barati.zehnsaz.ui.common.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InhibitoryControlViewModel @Inject constructor(
    private val sessionManager: GameSessionManager,
    private val soundManager: SoundManager
) : ViewModel() {

    private val engine = InhibitoryControlEngine()
    val uiState = engine.gameState

    private var roundJob: Job? = null
    private var isFinishing = false
    private var attemptStartTime = 0L

    private val _navigationEffect = MutableSharedFlow<ControlNavEffect>()
    val navigationEffect = _navigationEffect.asSharedFlow()

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.status == GameStatus.WON && !isFinishing) {
                    isFinishing = true
                    val results = calculateResults()
                    state.level?.let {
                        sessionManager.recordAttempt(it, isWon = true, startTime = attemptStartTime, results = results, trials = inhibitoryTrials(state))
                        sessionManager.completeLevel(it, results.stars, results.finalScore)
                        soundManager.play(SoundManager.SoundEvent.LEVEL_COMPLETE)
                        delay(1000)
                        _navigationEffect.emit(ControlNavEffect.ShowResults)
                    }
                }
                
                // Sound feedback
                state.lastFeedback?.let { feedback ->
                    // Only play if it's a new feedback
                    when (feedback) {
                        InhibitoryControlEngine.FeedbackType.CORRECT -> soundManager.play(SoundManager.SoundEvent.CORRECT)
                        InhibitoryControlEngine.FeedbackType.INCORRECT -> soundManager.play(SoundManager.SoundEvent.INCORRECT)
                    }
                }
            }
        }
    }

    fun loadLevel(level: Level) {
        isFinishing = false
        attemptStartTime = System.currentTimeMillis()
        engine.loadLevel(level)
    }

    fun startTraining() {
        startNextRound()
    }

    fun startNextRound() {
        viewModelScope.launch {
            engine.nextRound()
            startRoundTimer()
        }
    }

    private fun startRoundTimer() {
        val window = uiState.value.responseWindowMillis
        roundJob?.cancel()
        roundJob = viewModelScope.launch {
            delay(window)
            // If window ends and no action was taken
            if (uiState.value.status == GameStatus.PLAYING && uiState.value.waitingForResponse) {
                onUserAction(clicked = false)
            }
        }
    }

    fun onUserClick() {
        onUserAction(clicked = true)
    }

    private fun onUserAction(clicked: Boolean) {
        roundJob?.cancel()
        engine.onUserAction(clicked = clicked)
        
        // Auto advance after feedback delay
        viewModelScope.launch {
            delay(700)
            if (uiState.value.status == GameStatus.PLAYING) {
                startNextRound()
            }
        }
    }

    private fun inhibitoryTrials(state: InhibitoryControlEngine.InhibitoryState): List<TrialRecord> {
        return state.roundResults.mapIndexed { index, round ->
            TrialRecord(
                trialIndex = index,
                // The window is part of the stimulus label, so each trial's timing rule is stored without a schema change.
                stimulusLabel = "${round.stimulusType.name}|window=${round.responseWindowMillis}ms",
                responseLabel = round.responseType.name,
                isCorrect = round.isCorrect,
                reactionTimeMillis = round.reactionTimeMillis
            )
        }
    }

    fun calculateResults(): CognitiveScoreResult {
        val state = uiState.value
        val config = state.level?.config as? LevelConfig.InhibitoryControlConfig ?: return CognitiveScoreResult(0, 0, 0, 0)
        
        val res = InhibitoryControlScoreCalculator.calculate(
            results = state.roundResults,
            difficultyLevel = state.level.difficulty,
            responseWindowMillis = config.responseWindow
        )
        return CognitiveScoreResult(res.finalScore, res.noGoAccuracy, (res.goAccuracy / 2), res.stars)
    }
}

sealed class ControlNavEffect {
    object ShowResults : ControlNavEffect()
}
