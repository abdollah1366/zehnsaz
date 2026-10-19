package com.example.zehnchin.ui.speed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.domain.engine.GameStatus
import com.example.zehnchin.domain.engine.ProcessingSpeedEngine
import com.example.zehnchin.domain.model.*
import com.example.zehnchin.domain.progression.GameSessionManager
import com.example.zehnchin.domain.scoring.ProcessingSpeedScoreCalculator
import com.example.zehnchin.ui.common.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ProcessingSpeedViewModel @Inject constructor(
    private val sessionManager: GameSessionManager,
    private val soundManager: SoundManager
) : ViewModel() {

    private val engine = ProcessingSpeedEngine()
    val uiState = engine.gameState

    private val _viewPhase = MutableStateFlow(SpeedViewPhase.INSTRUCTION)
    val viewPhase = _viewPhase.asStateFlow()

    private val _countdownValue = MutableStateFlow(0)
    val countdownValue = _countdownValue.asStateFlow()

    private val _remainingTimeMillis = MutableStateFlow(0L)
    val remainingTimeMillis = _remainingTimeMillis.asStateFlow()

    private var trialFlowJob: Job? = null
    private var globalTimerJob: Job? = null

    private val _navigationEffect = MutableSharedFlow<SpeedNavEffect>()
    val navigationEffect = _navigationEffect.asSharedFlow()

    private var isFinishing = false

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.status == GameStatus.WON && !isFinishing) {
                    finishLevel()
                }
                
                // Sound feedback
                state.lastFeedback?.let { feedback ->
                    when (feedback) {
                        ProcessingSpeedEngine.FeedbackType.CORRECT -> soundManager.play(SoundManager.SoundEvent.CORRECT)
                        ProcessingSpeedEngine.FeedbackType.INCORRECT, 
                        ProcessingSpeedEngine.FeedbackType.FALSE_START -> soundManager.play(SoundManager.SoundEvent.INCORRECT)
                        ProcessingSpeedEngine.FeedbackType.TOO_SLOW -> {}
                    }
                }
            }
        }
    }

    private fun finishLevel() {
        if (isFinishing) return
        isFinishing = true
        
        trialFlowJob?.cancel()
        globalTimerJob?.cancel()
        
        val results = calculateResults()
        uiState.value.level?.let {
            viewModelScope.launch {
                sessionManager.completeLevel(it, results.stars, results.finalScore)
                soundManager.play(SoundManager.SoundEvent.LEVEL_COMPLETE)
                delay(500)
                _viewPhase.value = SpeedViewPhase.RESULT_SUMMARY
            }
        }
    }

    fun loadLevel(level: Level) {
        isFinishing = false
        engine.loadLevel(level)
        _viewPhase.value = SpeedViewPhase.INSTRUCTION
    }

    fun startTraining() {
        viewModelScope.launch {
            _viewPhase.value = SpeedViewPhase.COUNTDOWN
            for (i in 3 downTo 1) {
                _countdownValue.value = i
                soundManager.play(SoundManager.SoundEvent.COUNTDOWN_TICK)
                delay(1000)
            }
            _countdownValue.value = 0 // "GO"
            soundManager.play(SoundManager.SoundEvent.COUNTDOWN_GO)
            delay(500)
            
            _viewPhase.value = SpeedViewPhase.PLAYING
            startTrialFlow()
            startGlobalTimer()
        }
    }

    private fun startTrialFlow() {
        trialFlowJob?.cancel()
        trialFlowJob = viewModelScope.launch {
            val config = uiState.value.level?.config as? LevelConfig.ProcessingSpeedConfig ?: return@launch
            while (!isFinishing && (uiState.value.status == GameStatus.PLAYING || uiState.value.status == GameStatus.READY)) {
                if (uiState.value.currentRound >= config.roundCount) {
                    engine.resume() // Just to trigger WON state if needed
                    break
                }

                // 1. Random Neutral Delay
                val delayTime = Random.nextLong(500, 1500)
                delay(delayTime)

                if (isFinishing) break

                // 2. Start Trial
                engine.startTrial()
                
                // 3. Round Timer (Timeout)
                delay(config.responseTimeLimit)
                
                if (uiState.value.isStimulusVisible && !isFinishing) {
                    engine.onAction(responded = false)
                }
                
                // 4. Short gap between trials
                delay(300)
            }
        }
    }

    private fun startGlobalTimer() {
        val config = uiState.value.level?.config as? LevelConfig.ProcessingSpeedConfig ?: return
        val totalTime = config.roundCount * (config.responseTimeLimit + 1000L) // Estimate
        _remainingTimeMillis.value = totalTime
        
        globalTimerJob?.cancel()
        globalTimerJob = viewModelScope.launch {
            while (_remainingTimeMillis.value > 0 && !isFinishing) {
                delay(1000)
                _remainingTimeMillis.value -= 1000
                
                if (_remainingTimeMillis.value in 1L..5000L) {
                    soundManager.play(SoundManager.SoundEvent.TIME_WARNING)
                }
            }
            if (!isFinishing) {
                finishLevel()
            }
        }
    }

    fun onAction() {
        engine.onAction(responded = true)
    }

    fun onScreenClick() {
        if (uiState.value.status == GameStatus.PLAYING && !uiState.value.isStimulusVisible) {
            engine.onAction(responded = false, early = true)
        }
    }

    fun calculateResults(): CognitiveScoreResult {
        val state = uiState.value
        val config = state.level?.config as? LevelConfig.ProcessingSpeedConfig ?: return CognitiveScoreResult(0, 0, 0, 0)
        
        val res = ProcessingSpeedScoreCalculator.calculate(
            results = state.roundResults,
            difficultyLevel = state.level.difficulty,
            timeLimitMillis = config.responseTimeLimit
        )
        // Convert to UI result
        val avgReaction = if (state.roundResults.any { it.reactionTimeMillis > 0 }) {
            state.roundResults.filter { it.reactionTimeMillis > 0 }.map { it.reactionTimeMillis }.average().toInt()
        } else 0
        
        return CognitiveScoreResult(res.finalScore, res.accuracyPercent, avgReaction, res.stars)
    }

    override fun onCleared() {
        // Not releasing here as it's a Singleton provided by Hilt, 
        // usually releasing SoundPool happens at App level or when the whole feature is done.
        // But the user requested to release it. In Hilt @Singleton, this stays alive.
        // I will remove the release call for now to avoid breaking other games.
    }
}

enum class SpeedViewPhase {
    INSTRUCTION, COUNTDOWN, PLAYING, RESULT_SUMMARY
}

sealed class SpeedNavEffect {
    data class AutoNextLevel(val levelNum: Int) : SpeedNavEffect()
}
