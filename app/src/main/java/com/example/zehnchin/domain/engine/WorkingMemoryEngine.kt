package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.random.Random

class WorkingMemoryEngine : BaseGameEngine<WorkingMemoryEngine.MemoryState> {

    enum class MemoryPhase {
        ENCODING,
        RECALL,
        EVALUATION
    }

    enum class MemoryRule {
        SHAPE_ONLY,
        SHAPE_COLOR,
        SHAPE_COLOR_SIZE,
        ALL
    }

    enum class ErrorType {
        NONE, SHAPE, COLOR, SIZE, ORDER, LENGTH
    }

    data class MemoryState(
        val status: GameStatus = GameStatus.READY,
        val phase: MemoryPhase = MemoryPhase.ENCODING,
        val level: Level? = null,
        val targetSequence: List<AttentionStimulus> = emptyList(),
        val userSequence: List<AttentionStimulus> = emptyList(),
        val availableOptions: List<AttentionStimulus> = emptyList(),
        val recallStartTime: Long = 0,
        val totalRecallTimeMillis: Long = 0,
        val mistakeCount: Int = 0,
        val currentRule: MemoryRule = MemoryRule.SHAPE_ONLY,
        val lastError: ErrorType = ErrorType.NONE
    )

    private val _gameState = MutableStateFlow(MemoryState())
    override val gameState: StateFlow<MemoryState> = _gameState.asStateFlow()

    override fun loadLevel(level: Level) {
        val config = level.config as? LevelConfig.WorkingMemoryConfig ?: return
        
        val rule = when (level.levelNumber) {
            in 1..3 -> MemoryRule.SHAPE_ONLY
            in 4..7 -> MemoryRule.SHAPE_COLOR
            in 8..12 -> MemoryRule.SHAPE_COLOR_SIZE
            else -> MemoryRule.ALL
        }

        val random = kotlin.random.Random(level.id)
        val sequence = generateRandomSequence(config, random, rule)
        val options = generateOptions(sequence, config, random, rule)
        
        _gameState.update {
            it.copy(
                status = GameStatus.READY,
                phase = MemoryPhase.ENCODING,
                level = level,
                targetSequence = sequence,
                userSequence = emptyList(),
                availableOptions = options.shuffled(random),
                mistakeCount = 0,
                totalRecallTimeMillis = 0,
                currentRule = rule
            )
        }
    }

    private val sizes = listOf(0.7f, 1.0f, 1.3f)

    private fun generateRandomSequence(
        config: LevelConfig.WorkingMemoryConfig, 
        random: kotlin.random.Random,
        rule: MemoryRule
    ): List<AttentionStimulus> {
        return (1..config.memoryLength).map {
            AttentionStimulus(
                color = if (rule == MemoryRule.SHAPE_ONLY) PuzzleColor.BLUE else config.stimuliColors.random(random),
                shape = config.stimuliShapes.random(random),
                scale = if (rule == MemoryRule.SHAPE_ONLY || rule == MemoryRule.SHAPE_COLOR) 1.0f else sizes.random(random)
            )
        }
    }

    private fun generateOptions(
        sequence: List<AttentionStimulus>, 
        config: LevelConfig.WorkingMemoryConfig,
        random: kotlin.random.Random,
        rule: MemoryRule
    ): List<AttentionStimulus> {
        val options = sequence.toMutableSet()
        while (options.size < config.numberOfChoices) {
            val s = AttentionStimulus(
                color = if (rule == MemoryRule.SHAPE_ONLY) PuzzleColor.BLUE else config.stimuliColors.random(random),
                shape = config.stimuliShapes.random(random),
                scale = if (rule == MemoryRule.SHAPE_ONLY || rule == MemoryRule.SHAPE_COLOR) 1.0f else sizes.random(random)
            )
            // Ensure we don't add a duplicate distractor that looks like a target if we want uniqueness
            if (options.none { it.matchesRule(s, rule) }) {
                options.add(s)
            }
        }
        return options.toList()
    }

    fun startEncoding() {
        _gameState.update { it.copy(status = GameStatus.PLAYING, phase = MemoryPhase.ENCODING) }
    }

    fun startRecall() {
        _gameState.update { 
            it.copy(
                phase = MemoryPhase.RECALL, 
                recallStartTime = System.currentTimeMillis()
            ) 
        }
    }

    fun onOptionSelected(stimulus: AttentionStimulus) {
        if (_gameState.value.phase != MemoryPhase.RECALL) return
        
        _gameState.update { it.copy(userSequence = it.userSequence + stimulus) }
    }

    fun removeLastSelected() {
        if (_gameState.value.phase != MemoryPhase.RECALL) return
        _gameState.update { it.copy(userSequence = it.userSequence.dropLast(1)) }
    }

    fun submitAnswer() {
        val state = _gameState.value
        val config = state.level?.config as? LevelConfig.WorkingMemoryConfig ?: return
        
        val now = System.currentTimeMillis()
        val recallTime = now - state.recallStartTime
        
        var errorType = ErrorType.NONE
        val isCorrect = if (state.userSequence.size != state.targetSequence.size) {
            errorType = ErrorType.LENGTH
            false
        } else {
            var correct = true
            for (i in state.targetSequence.indices) {
                val t = state.targetSequence[i]
                val u = state.userSequence[i]
                
                if (t.shape != u.shape) {
                    errorType = ErrorType.SHAPE
                    correct = false
                    break
                }
                if (state.currentRule != MemoryRule.SHAPE_ONLY && t.color != u.color) {
                    errorType = ErrorType.COLOR
                    correct = false
                    break
                }
                if ((state.currentRule == MemoryRule.SHAPE_COLOR_SIZE || state.currentRule == MemoryRule.ALL) && 
                    abs(t.scale - u.scale) > 0.01f) {
                    errorType = ErrorType.SIZE
                    correct = false
                    break
                }
            }
            correct
        }
        
        _gameState.update {
            it.copy(
                status = if (isCorrect) GameStatus.WON else GameStatus.LOST,
                phase = MemoryPhase.EVALUATION,
                totalRecallTimeMillis = recallTime,
                mistakeCount = if (isCorrect) it.mistakeCount else it.mistakeCount + 1,
                lastError = errorType
            )
        }
    }

    override fun reset() {
        _gameState.update { MemoryState() }
    }

    override fun pause() {
        _gameState.update { it.copy(status = GameStatus.PAUSED) }
    }

    override fun resume() {
        _gameState.update { it.copy(status = GameStatus.PLAYING) }
    }
}