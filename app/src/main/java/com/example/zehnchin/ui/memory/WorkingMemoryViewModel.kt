package com.example.zehnchin.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.data.local.dao.LevelDao
import com.example.zehnchin.domain.engine.GameStatus
import com.example.zehnchin.domain.engine.WorkingMemoryEngine
import com.example.zehnchin.domain.model.*
import com.example.zehnchin.domain.progression.GameSessionManager
import com.example.zehnchin.domain.scoring.WorkingMemoryScoreCalculator
import com.example.zehnchin.ui.common.SoundManager
import com.example.zehnchin.domain.economy.RewardEngine
import com.example.zehnchin.domain.monetization.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkingMemoryViewModel @Inject constructor(
    private val sessionManager: GameSessionManager,
    private val soundManager: SoundManager,
    private val levelDao: LevelDao,
    private val rewardEngine: RewardEngine,
    private val adManager: AdManager
) : ViewModel() {

    private val engine = WorkingMemoryEngine()
    val uiState = engine.gameState

    private val _timerValue = MutableStateFlow(0L)
    val timerValue = _timerValue.asStateFlow()

    private val _navigationEffect = MutableSharedFlow<MemoryNavEffect>()
    val navigationEffect = _navigationEffect.asSharedFlow()

    private val _isNextLevelUnlocked = MutableStateFlow(false)
    val isNextLevelUnlocked = _isNextLevelUnlocked.asStateFlow()

    private var isFinishing = false
    private var attemptStartTime = 0L

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.status == GameStatus.WON && !isFinishing) {
                    isFinishing = true
                    val results = calculateResults()
                    state.level?.let {
                        sessionManager.recordAttempt(it, isWon = true, startTime = attemptStartTime, results = results, trials = memoryTrials(state))
                        sessionManager.completeLevel(it, results.stars, results.finalScore)
                        soundManager.play(SoundManager.SoundEvent.LEVEL_COMPLETE)
                        // Show feedback and auto-navigate
                        delay(1200)
                        _navigationEffect.emit(MemoryNavEffect.AutoNextLevel(it.levelNumber + 1))
                    }
                } else if (state.status == GameStatus.LOST && !isFinishing) {
                    isFinishing = true
                    soundManager.play(SoundManager.SoundEvent.LEVEL_FAILED)
                    state.level?.let {
                        sessionManager.recordAttempt(it, isWon = false, startTime = attemptStartTime, results = calculateResults(), trials = memoryTrials(state))
                        checkNextLevelUnlocked(it.levelNumber + 1)
                    }
                }
            }
        }
    }

    private fun checkNextLevelUnlocked(nextLevelNum: Int) {
        viewModelScope.launch {
            val currentLevelId = 2000L + (nextLevelNum - 1)
            val progress = levelDao.getLevelProgress(currentLevelId).first()
            _isNextLevelUnlocked.value = progress?.isCompleted == true
        }
    }

    fun loadLevel(level: Level) {
        isFinishing = false
        attemptStartTime = System.currentTimeMillis()
        engine.loadLevel(level)
    }

    fun retryLevel() {
        soundManager.play(SoundManager.SoundEvent.BUTTON_CLICK)
        uiState.value.level?.let {
            isFinishing = false
            attemptStartTime = System.currentTimeMillis()
            engine.loadLevel(it)
            startSession()
        }
    }

    fun skipLevelWithCoins() {
        viewModelScope.launch {
            val success = rewardEngine.spendCoins(50, RewardSource.OTHER)
            if (success) {
                unlockAndGoNext()
            }
        }
    }

    fun skipLevelWithAd() {
        adManager.showRewardedAd(AdPlacement.LEVEL_FAILED) { result ->
            if (result == AdResult.COMPLETED) {
                unlockAndGoNext()
            }
        }
    }

    private fun unlockAndGoNext() {
        uiState.value.level?.let { currentLevel ->
            viewModelScope.launch {
                sessionManager.completeLevel(currentLevel, 0, 0)
                _navigationEffect.emit(MemoryNavEffect.AutoNextLevel(currentLevel.levelNumber + 1))
            }
        }
    }

    fun startSession() {
        val config = uiState.value.level?.config as? LevelConfig.WorkingMemoryConfig ?: return
        
        viewModelScope.launch {
            engine.startEncoding()
            
            // Encoding Phase Timer
            var remainingEncoding = config.encodingDuration
            while (remainingEncoding > 0 && uiState.value.phase == WorkingMemoryEngine.MemoryPhase.ENCODING) {
                _timerValue.value = remainingEncoding
                delay(100)
                remainingEncoding -= 100
                
                // Tick in last 3 seconds
                if (remainingEncoding in 100L..3000L && remainingEncoding % 1000 == 0L) {
                    soundManager.play(SoundManager.SoundEvent.COUNTDOWN_TICK)
                }
            }
            
            if (uiState.value.phase == WorkingMemoryEngine.MemoryPhase.ENCODING) {
                soundManager.play(SoundManager.SoundEvent.COUNTDOWN_GO)
                engine.startRecall()
                startRecallTimer()
            }
        }
    }

    private fun startRecallTimer() {
        val config = uiState.value.level?.config as? LevelConfig.WorkingMemoryConfig ?: return
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (uiState.value.phase == WorkingMemoryEngine.MemoryPhase.RECALL) {
                val elapsed = System.currentTimeMillis() - startTime
                _timerValue.value = config.recallTimeLimit - elapsed
                if (_timerValue.value <= 0) {
                    engine.submitAnswer()
                    break
                }
                delay(100)
            }
        }
    }

    fun onOptionSelected(stimulus: AttentionStimulus) {
        soundManager.play(SoundManager.SoundEvent.BUTTON_CLICK)
        engine.onOptionSelected(stimulus)
    }

    fun removeLastSelected() {
        soundManager.play(SoundManager.SoundEvent.UNDO)
        engine.removeLastSelected()
    }

    fun submitAnswer() {
        engine.submitAnswer()
    }

    /** One trial per target position: what was expected there and what the player picked. */
    private fun memoryTrials(state: WorkingMemoryEngine.MemoryState): List<TrialRecord> {
        return state.targetSequence.mapIndexed { index, target ->
            val answer = state.userSequence.getOrNull(index)
            TrialRecord(
                trialIndex = index,
                stimulusLabel = "${target.color}_${target.shape}_scale${target.scale}",
                responseLabel = answer?.let { "${it.color}_${it.shape}_scale${it.scale}" } ?: "NO_RESPONSE",
                isCorrect = answer == target,
                reactionTimeMillis = null
            )
        }
    }

    fun calculateResults(): CognitiveScoreResult {
        val state = uiState.value
        val config = state.level?.config as? LevelConfig.WorkingMemoryConfig ?: return CognitiveScoreResult(0, 0, 0, 0)
        
        var correctItems = 0
        state.userSequence.forEachIndexed { index, stimulus ->
            if (index < state.targetSequence.size && stimulus == state.targetSequence[index]) {
                correctItems++
            }
        }

        val res = WorkingMemoryScoreCalculator.calculate(
            correctItems = correctItems,
            totalItems = state.targetSequence.size,
            recallTimeMillis = state.totalRecallTimeMillis,
            difficultyLevel = state.level.difficulty,
            timeLimitMillis = config.recallTimeLimit,
            sequenceLength = state.targetSequence.size
        )
        return CognitiveScoreResult(res.finalScore, res.accuracyPercent, res.recallSpeedScore, res.stars)
    }
}

sealed class MemoryNavEffect {
    data class AutoNextLevel(val levelNum: Int) : MemoryNavEffect()
}
