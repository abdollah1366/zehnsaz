package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class SelectiveAttentionEngine : BaseGameEngine<SelectiveAttentionEngine.AttentionState> {

    data class AttentionState(
        val status: GameStatus = GameStatus.READY,
        val level: Level? = null,
        val stimuli: List<AttentionStimulus> = emptyList(),
        val targetColor: PuzzleColor = PuzzleColor.RED,
        val targetShape: Shape = Shape.CIRCLE,
        val targetScale: Float? = null,
        val correctCount: Int = 0,
        val mistakeCount: Int = 0,
        val totalReactionTimeMillis: Long = 0,
        val currentRoundStartTime: Long = 0,
        val moves: Int = 0,
        val currentTrial: Int = 0,
        val lastFeedback: FeedbackType? = null
    )

    enum class FeedbackType {
        CORRECT, INCORRECT
    }

    private val _gameState = MutableStateFlow(AttentionState())
    override val gameState: StateFlow<AttentionState> = _gameState.asStateFlow()

    override fun loadLevel(level: Level) {
        val config = level.config as? LevelConfig.SelectiveAttentionConfig ?: return
        
        _gameState.update {
            it.copy(
                status = GameStatus.READY,
                level = level,
                targetColor = config.targetColor,
                targetShape = config.targetShape,
                targetScale = config.targetScale,
                correctCount = 0,
                mistakeCount = 0,
                totalReactionTimeMillis = 0,
                moves = 0,
                currentTrial = 0,
                lastFeedback = null
            )
        }
        generateRound()
    }

    fun startRound() {
        _gameState.update { it.copy(status = GameStatus.PLAYING, currentRoundStartTime = System.currentTimeMillis()) }
    }

    fun generateRound() {
        val level = _gameState.value.level ?: return
        val config = level.config as LevelConfig.SelectiveAttentionConfig
        
        val totalCount = config.gridRows * config.gridCols
        val stimuliList = mutableListOf<AttentionStimulus>()
        
        val sizes = listOf(0.7f, 1.0f, 1.3f)

        // Add targets
        repeat(config.targetCount) {
            stimuliList.add(
                AttentionStimulus(
                    color = config.targetColor, 
                    shape = config.targetShape, 
                    isTarget = true,
                    scale = config.targetScale ?: 1.0f
                )
            )
        }
        
        // Add distractors
        val colors = PuzzleColor.entries
        val shapes = Shape.entries
        
        while (stimuliList.size < totalCount) {
            val randomColor = colors[Random.nextInt(colors.size)]
            val randomShape = shapes[Random.nextInt(shapes.size)]
            val randomScale = sizes.random()
            
            // Ensure distractor is not identical to target
            val isTargetConfig = randomColor == config.targetColor && 
                               randomShape == config.targetShape && 
                               (config.targetScale == null || Math.abs(randomScale - config.targetScale) < 0.01f)

            if (!isTargetConfig) {
                stimuliList.add(AttentionStimulus(color = randomColor, shape = randomShape, isTarget = false, scale = randomScale))
            }
        }
        
        _gameState.update { 
            it.copy(
                stimuli = stimuliList.shuffled(), 
                status = GameStatus.READY,
                targetColor = config.targetColor,
                targetShape = config.targetShape,
                targetScale = config.targetScale,
                lastFeedback = null
            ) 
        }
    }

    fun onStimulusClicked(stimulusId: String) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return
        
        val config = state.level?.config as? LevelConfig.SelectiveAttentionConfig ?: return
        val now = System.currentTimeMillis()
        val reactionTime = now - state.currentRoundStartTime
        
        val stimulus = state.stimuli.find { it.id == stimulusId } ?: return
        
        if (stimulus.isTarget) {
            val newCorrectCount = state.correctCount + 1
            val newTrial = state.currentTrial + 1
            val isFinished = newTrial >= config.trialCount
            
            _gameState.update { 
                it.copy(
                    correctCount = newCorrectCount,
                    totalReactionTimeMillis = it.totalReactionTimeMillis + reactionTime,
                    moves = it.moves + 1,
                    currentTrial = newTrial,
                    lastFeedback = FeedbackType.CORRECT,
                    status = if (isFinished) GameStatus.WON else GameStatus.READY
                )
            }
        } else {
            val newTrial = state.currentTrial + 1
            val isFinished = newTrial >= config.trialCount

            _gameState.update { 
                it.copy(
                    mistakeCount = it.mistakeCount + 1,
                    moves = it.moves + 1,
                    currentTrial = newTrial,
                    lastFeedback = FeedbackType.INCORRECT,
                    status = if (isFinished) GameStatus.WON else GameStatus.READY
                )
            }
        }
    }

    override fun reset() {
        _gameState.update { AttentionState() }
    }

    override fun pause() {
        _gameState.update { it.copy(status = GameStatus.PAUSED) }
    }

    override fun resume() {
        _gameState.update { it.copy(status = GameStatus.PLAYING) }
    }
}