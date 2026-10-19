package com.example.zehnchin.ui.attention

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.domain.engine.GameStatus
import com.example.zehnchin.domain.engine.SelectiveAttentionEngine
import com.example.zehnchin.domain.model.*
import com.example.zehnchin.domain.progression.GameSessionManager
import com.example.zehnchin.domain.scoring.AttentionScoreCalculator
import com.example.zehnchin.ui.common.SoundManager
import com.example.zehnchin.domain.economy.RewardEngine
import com.example.zehnchin.domain.monetization.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectiveAttentionViewModel @Inject constructor(
    private val sessionManager: GameSessionManager,
    private val soundManager: SoundManager,
    private val levelDao: com.example.zehnchin.data.local.dao.LevelDao,
    private val rewardEngine: RewardEngine,
    private val adManager: AdManager
) : ViewModel() {

    private val engine = SelectiveAttentionEngine()
    val uiState = engine.gameState

    private val _timerValue = MutableStateFlow(0L)
    val timerValue = _timerValue.asStateFlow()

    private val _navigationEffect = MutableSharedFlow<AttentionNavEffect>()
    val navigationEffect = _navigationEffect.asSharedFlow()

    private val _isNextLevelUnlocked = MutableStateFlow(false)
    val isNextLevelUnlocked = _isNextLevelUnlocked.asStateFlow()

    private var isFinishing = false

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.status == GameStatus.WON && !isFinishing) {
                    finishLevel()
                } else if (state.status == GameStatus.LOST && !isFinishing) {
                    soundManager.play(SoundManager.SoundEvent.LEVEL_FAILED)
                    state.level?.let { checkNextLevelUnlocked(it.levelNumber + 1) }
                }
                
                // Auto-advance logic for answers within trial
                if (state.lastFeedback != null && state.status == GameStatus.READY && !isFinishing) {
                    delay(800)
                    if (!isFinishing) {
                        engine.generateRound()
                        engine.startRound()
                    }
                }
            }
        }
    }

    private fun checkNextLevelUnlocked(nextLevelNum: Int) {
        viewModelScope.launch {
            val nextLevelId = 1000L + nextLevelNum
            val progress = levelDao.getLevelProgress(nextLevelId).first()
            _isNextLevelUnlocked.value = progress?.isCompleted == true
        }
    }

    private fun finishLevel() {
        isFinishing = true
        val results = calculateResults()
        uiState.value.level?.let {
            viewModelScope.launch {
                sessionManager.completeLevel(it, results.stars, results.finalScore)
                soundManager.play(SoundManager.SoundEvent.LEVEL_COMPLETE)
                delay(1200)
                _navigationEffect.emit(AttentionNavEffect.ShowResults)
            }
        }
    }

    fun loadLevel(level: Level) {
        isFinishing = false
        engine.loadLevel(level)
        // Auto start
        viewModelScope.launch {
            delay(1000)
            engine.startRound()
            startTimer()
        }
    }

    fun retryLevel() {
        soundManager.play(SoundManager.SoundEvent.BUTTON_CLICK)
        uiState.value.level?.let {
            isFinishing = false
            engine.loadLevel(it)
            viewModelScope.launch {
                delay(500)
                engine.startRound()
                startTimer()
            }
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
                _navigationEffect.emit(AttentionNavEffect.AutoNextLevel(currentLevel.levelNumber + 1))
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (uiState.value.status == GameStatus.PLAYING && !isFinishing) {
                _timerValue.value = System.currentTimeMillis() - startTime
                delay(100)
            }
        }
    }

    fun onStimulusClick(stimulusId: String) {
        if (isFinishing) return
        
        val stimulus = uiState.value.stimuli.find { it.id == stimulusId }
        if (stimulus?.isTarget == true) {
            soundManager.play(SoundManager.SoundEvent.CORRECT)
        } else {
            soundManager.play(SoundManager.SoundEvent.INCORRECT)
        }
        engine.onStimulusClicked(stimulusId)
    }

    fun calculateResults(): CognitiveScoreResult {
        val state = uiState.value
        val config = state.level?.config as? LevelConfig.SelectiveAttentionConfig
        val res = AttentionScoreCalculator.calculate(
            correctAnswers = state.correctCount,
            totalAttempts = state.moves,
            totalReactionTimeMillis = state.totalReactionTimeMillis,
            difficultyLevel = state.level?.difficulty ?: 1,
            timeLimitMillis = config?.responseTimeLimit ?: 10000
        )
        return CognitiveScoreResult(res.finalScore, res.accuracyPercent, res.reactionSpeedScore, res.stars)
    }
}

sealed class AttentionNavEffect {
    object ShowResults : AttentionNavEffect()
    data class AutoNextLevel(val levelNum: Int) : AttentionNavEffect()
}
