package com.example.zehnchin.ui.flexibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.domain.engine.GameStatus
import com.example.zehnchin.domain.engine.CognitiveFlexibilityEngine
import com.example.zehnchin.domain.model.*
import com.example.zehnchin.domain.progression.GameSessionManager
import com.example.zehnchin.domain.scoring.FlexibilityScoreCalculator
import com.example.zehnchin.ui.common.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlexibilityViewModel @Inject constructor(
    private val sessionManager: GameSessionManager,
    private val soundManager: SoundManager
) : ViewModel() {

    private val engine = CognitiveFlexibilityEngine()
    val uiState = engine.gameState

    private var roundTimerJob: Job? = null
    private var isFinishing = false
    private var attemptStartTime = 0L

    private val _navigationEffect = MutableSharedFlow<FlexibilityNavEffect>()
    val navigationEffect = _navigationEffect.asSharedFlow()

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.status == GameStatus.WON && !isFinishing) {
                    isFinishing = true
                    val results = calculateResults()
                    state.level?.let {
                        sessionManager.recordAttempt(it, isWon = true, startTime = attemptStartTime, results = results, trials = flexibilityTrials(state))
                        sessionManager.completeLevel(it, results.stars, results.finalScore)
                        soundManager.play(SoundManager.SoundEvent.LEVEL_COMPLETE)
                        delay(1000)
                        _navigationEffect.emit(FlexibilityNavEffect.ShowResults)
                    }
                }
                
                if (state.isSwitchingRule) {
                    soundManager.play(SoundManager.SoundEvent.COUNTDOWN_TICK)
                }

                state.lastFeedback?.let { feedback ->
                    when (feedback) {
                        CognitiveFlexibilityEngine.FeedbackType.CORRECT -> soundManager.play(SoundManager.SoundEvent.CORRECT)
                        CognitiveFlexibilityEngine.FeedbackType.INCORRECT -> soundManager.play(SoundManager.SoundEvent.INCORRECT)
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
        val config = uiState.value.level?.config as? LevelConfig.CognitiveFlexibilityConfig ?: return
        roundTimerJob?.cancel()
        roundTimerJob = viewModelScope.launch {
            delay(config.responseTimeLimit)
            if (uiState.value.status == GameStatus.PLAYING && uiState.value.isStimulusVisible) {
                onResponse("TIMEOUT")
            }
        }
    }

    fun onResponse(response: Any) {
        roundTimerJob?.cancel()
        engine.onUserAction(userResponse = response)
        
        // Auto advance
        viewModelScope.launch {
            delay(800)
            if (uiState.value.status == GameStatus.PLAYING) {
                startNextRound()
            }
        }
    }

    private fun flexibilityTrials(state: CognitiveFlexibilityEngine.FlexibilityState): List<TrialRecord> {
        return state.roundResults.mapIndexed { index, round ->
            TrialRecord(
                trialIndex = index,
                stimulusLabel = "${round.ruleType}:${round.stimulus.color}_${round.stimulus.shape}",
                responseLabel = round.userResponse,
                isCorrect = round.isCorrect,
                reactionTimeMillis = round.reactionTimeMillis
            )
        }
    }

    fun calculateResults(): CognitiveScoreResult {
        val state = uiState.value
        val config = state.level?.config as? LevelConfig.CognitiveFlexibilityConfig ?: return CognitiveScoreResult(0, 0, 0, 0)
        
        val res = FlexibilityScoreCalculator.calculate(
            results = state.roundResults,
            difficultyLevel = state.level.difficulty,
            timeLimitMillis = config.responseTimeLimit
        )
        return CognitiveScoreResult(res.finalScore, res.accuracyPercent, res.postSwitchAccuracy, res.stars)
    }
}

sealed class FlexibilityNavEffect {
    object ShowResults : FlexibilityNavEffect()
}
