package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class ProcessingSpeedEngine : BaseGameEngine<ProcessingSpeedEngine.SpeedState> {

    data class SpeedState(
        val status: GameStatus = GameStatus.READY,
        val level: Level? = null,
        val currentRound: Int = 0,
        val currentStimulus: AttentionStimulus? = null,
        val targetCriteria: Pair<PuzzleColor, Shape>? = null,
        val roundResults: List<ProcessingRoundResult> = emptyList(),
        val currentCombo: Int = 0,
        val maxCombo: Int = 0,
        val roundStartTime: Long = 0,
        val isStimulusVisible: Boolean = false,
        val lastFeedback: FeedbackType? = null
    )

    enum class FeedbackType {
        CORRECT, INCORRECT, FALSE_START, TOO_SLOW
    }

    private val _gameState = MutableStateFlow(SpeedState())
    override val gameState: StateFlow<SpeedState> = _gameState.asStateFlow()

    override fun loadLevel(level: Level) {
        val config = level.config as? LevelConfig.ProcessingSpeedConfig ?: return
        
        val random = Random(level.id)
        val targetColor = PuzzleColor.entries.random(random)
        val targetShape = Shape.entries.random(random)

        _gameState.update {
            it.copy(
                status = GameStatus.READY,
                level = level,
                currentRound = 0,
                targetCriteria = targetColor to targetShape,
                roundResults = emptyList(),
                currentCombo = 0,
                maxCombo = 0,
                isStimulusVisible = false,
                lastFeedback = null
            )
        }
    }

    fun startTrial() {
        val state = _gameState.value
        val config = state.level?.config as? LevelConfig.ProcessingSpeedConfig ?: return
        
        if (state.currentRound >= config.roundCount) {
            _gameState.update { it.copy(status = GameStatus.WON) }
            return
        }

        val target = state.targetCriteria ?: return
        // Use a random that's not strictly tied to level.id for round variety, 
        // or combined with round number for determinism.
        val roundRandom = Random(state.level!!.id + state.currentRound)
        val isTarget = roundRandom.nextFloat() < config.targetProbability
        
        val stimulus = if (isTarget) {
            AttentionStimulus(color = target.first, shape = target.second, isTarget = true)
        } else {
            var randomColor: PuzzleColor
            var randomShape: Shape
            do {
                randomColor = PuzzleColor.entries.random(roundRandom)
                randomShape = Shape.entries.random(roundRandom)
            } while (randomColor == target.first && randomShape == target.second)
            
            AttentionStimulus(color = randomColor, shape = randomShape, isTarget = false)
        }

        _gameState.update {
            it.copy(
                currentRound = it.currentRound + 1,
                currentStimulus = stimulus,
                isStimulusVisible = true,
                roundStartTime = System.currentTimeMillis(),
                status = GameStatus.PLAYING,
                lastFeedback = null
            )
        }
    }

    fun onAction(responded: Boolean, early: Boolean = false) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return

        if (early) {
            recordRoundResult(ResponseType.INCORRECT, 0, FeedbackType.FALSE_START)
            return
        }

        if (!state.isStimulusVisible) return

        val now = System.currentTimeMillis()
        val reactionTime = now - state.roundStartTime
        val stimulus = state.currentStimulus ?: return
        val target = state.targetCriteria ?: return

        val isTarget = stimulus.color == target.first && stimulus.shape == target.second
        
        val responseType = when {
            responded && isTarget -> ResponseType.CORRECT
            responded && !isTarget -> ResponseType.INCORRECT
            !responded && isTarget -> ResponseType.TIMEOUT
            else -> ResponseType.CORRECT // Correctly ignored a distractor (if we support that logic)
        }

        val feedback = when (responseType) {
            ResponseType.CORRECT -> FeedbackType.CORRECT
            ResponseType.INCORRECT -> FeedbackType.INCORRECT
            ResponseType.TIMEOUT -> FeedbackType.TOO_SLOW
        }

        recordRoundResult(responseType, reactionTime, feedback)
    }

    private fun recordRoundResult(responseType: ResponseType, reactionTime: Long, feedback: FeedbackType) {
        val state = _gameState.value
        val isCorrect = responseType == ResponseType.CORRECT
        val newCombo = if (isCorrect) state.currentCombo + 1 else 0
        
        val roundResult = ProcessingRoundResult(
            roundNumber = state.currentRound,
            stimulus = state.currentStimulus ?: AttentionStimulus(color = PuzzleColor.RED, shape = Shape.CIRCLE),
            responseType = responseType,
            reactionTimeMillis = reactionTime,
            score = 0,
            combo = newCombo
        )

        _gameState.update {
            it.copy(
                roundResults = it.roundResults + roundResult,
                currentCombo = newCombo,
                maxCombo = maxOf(it.maxCombo, newCombo),
                isStimulusVisible = false,
                lastFeedback = feedback
            )
        }
    }

    override fun reset() {
        _gameState.update { SpeedState() }
    }

    override fun pause() {
        _gameState.update { it.copy(status = GameStatus.PAUSED) }
    }

    override fun resume() {
        _gameState.update { it.copy(status = GameStatus.PLAYING) }
    }
}
