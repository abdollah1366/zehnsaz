package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class InhibitoryControlEngine : BaseGameEngine<InhibitoryControlEngine.InhibitoryState> {

    data class InhibitoryState(
        val status: GameStatus = GameStatus.READY,
        val level: Level? = null,
        val currentRound: Int = 0,
        val currentStimulus: AttentionStimulus? = null,
        val stimulusType: InhibitoryStimulusType = InhibitoryStimulusType.GO,
        val roundResults: List<InhibitoryRoundResult> = emptyList(),
        val currentCombo: Int = 0,
        val maxCombo: Int = 0,
        val roundStartTime: Long = 0,
        val responseWindowMillis: Long = 0L,
        val isStimulusVisible: Boolean = false,
        val waitingForResponse: Boolean = false,
        val lastFeedback: FeedbackType? = null
    )

    enum class FeedbackType {
        CORRECT, INCORRECT
    }

    private val _gameState = MutableStateFlow(InhibitoryState())
    override val gameState: StateFlow<InhibitoryState> = _gameState.asStateFlow()

    override fun loadLevel(level: Level) {
        _gameState.update {
            it.copy(
                status = GameStatus.READY,
                level = level,
                currentRound = 0,
                roundResults = emptyList(),
                currentCombo = 0,
                maxCombo = 0,
                isStimulusVisible = false,
                waitingForResponse = false,
                responseWindowMillis = (level.config as? LevelConfig.InhibitoryControlConfig)?.responseWindow ?: 0L
            )
        }
    }

    fun nextRound() {
        val state = _gameState.value
        val config = state.level?.config as? LevelConfig.InhibitoryControlConfig ?: return

        if (state.currentRound >= config.roundCount) {
            _gameState.update { it.copy(status = GameStatus.WON) }
            return
        }

        val isGo = Random.nextFloat() < config.goProbability
        val stimulusType = if (isGo) InhibitoryStimulusType.GO else InhibitoryStimulusType.NO_GO

        val stimulus = if (isGo) {
            // Same colour for both stimuli: only the shape tells Go from No-Go, so colour-blind players are not disadvantaged.
            AttentionStimulus(color = PuzzleColor.BLUE, shape = Shape.CIRCLE, isTarget = true)
        } else {
            AttentionStimulus(color = PuzzleColor.BLUE, shape = Shape.SQUARE, isTarget = false)
        }

        _gameState.update {
            it.copy(
                currentRound = it.currentRound + 1,
                currentStimulus = stimulus,
                stimulusType = stimulusType,
                isStimulusVisible = true,
                waitingForResponse = true,
                roundStartTime = System.currentTimeMillis(),
                status = GameStatus.PLAYING
            )
        }
    }

    fun onUserAction(clicked: Boolean) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING || !state.waitingForResponse) return

        val now = System.currentTimeMillis()
        val reactionTime = now - state.roundStartTime
        
        val responseType = when {
            state.stimulusType == InhibitoryStimulusType.GO && clicked -> InhibitoryResponseType.GO_CORRECT
            state.stimulusType == InhibitoryStimulusType.GO && !clicked -> InhibitoryResponseType.GO_MISSED
            state.stimulusType == InhibitoryStimulusType.NO_GO && clicked -> InhibitoryResponseType.NO_GO_INCORRECT
            state.stimulusType == InhibitoryStimulusType.NO_GO && !clicked -> InhibitoryResponseType.NO_GO_CORRECT
            else -> InhibitoryResponseType.GO_INCORRECT // Catch-all
        }

        val isCorrect = responseType == InhibitoryResponseType.GO_CORRECT || responseType == InhibitoryResponseType.NO_GO_CORRECT
        val newCombo = if (isCorrect) state.currentCombo + 1 else 0

        val result = InhibitoryRoundResult(
            roundNumber = state.currentRound,
            stimulusType = state.stimulusType,
            responseType = responseType,
            reactionTimeMillis = if (clicked) reactionTime else null,
            isCorrect = isCorrect,
            score = 0, // Computed elsewhere
            combo = newCombo,
            responseWindowMillis = state.responseWindowMillis
        )

        // Only Go trials move the window: a No-Go trial says nothing about how fast the player can respond.
        val nextWindow = if (state.stimulusType == InhibitoryStimulusType.GO) {
            ResponseWindowStaircase(state.responseWindowMillis)
                .afterGoTrial(hit = responseType == InhibitoryResponseType.GO_CORRECT)
                .windowMillis
        } else {
            state.responseWindowMillis
        }

        _gameState.update {
            it.copy(
                responseWindowMillis = nextWindow,
                roundResults = it.roundResults + result,
                currentCombo = newCombo,
                maxCombo = maxOf(it.maxCombo, newCombo),
                isStimulusVisible = false,
                waitingForResponse = false,
                lastFeedback = if (isCorrect) FeedbackType.CORRECT else FeedbackType.INCORRECT
            )
        }
    }

    override fun reset() {
        _gameState.update { InhibitoryState() }
    }

    override fun pause() {
        _gameState.update { it.copy(status = GameStatus.PAUSED) }
    }

    override fun resume() {
        _gameState.update { it.copy(status = GameStatus.PLAYING) }
    }
}