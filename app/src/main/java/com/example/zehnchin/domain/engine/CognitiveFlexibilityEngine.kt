package com.example.zehnchin.domain.engine

import com.example.zehnchin.R
import com.example.zehnchin.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class CognitiveFlexibilityEngine : BaseGameEngine<CognitiveFlexibilityEngine.FlexibilityState> {

    data class FlexibilityState(
        val status: GameStatus = GameStatus.READY,
        val level: Level? = null,
        val currentRound: Int = 0,
        val currentStimulus: AttentionStimulus? = null,
        val currentRule: CognitiveRule? = null,
        val roundResults: List<FlexibilityRoundResult> = emptyList(),
        val currentCombo: Int = 0,
        val maxCombo: Int = 0,
        val roundStartTime: Long = 0,
        val isStimulusVisible: Boolean = false,
        val switchCount: Int = 0,
        val isSwitchingRule: Boolean = false,
        val postSwitchTrialsLeft: Int = 0,
        val lastFeedback: FeedbackType? = null,
        val availableOptions: List<Any> = emptyList()
    )

    enum class FeedbackType {
        CORRECT, INCORRECT
    }

    companion object {
        /**
         * Trials after a rule switch that count as post-switch. Using the first few, not only the first,
         * gives the switch-cost measure more data than a single trial.
         */
        const val POST_SWITCH_WINDOW = 3
    }

    private val _gameState = MutableStateFlow(FlexibilityState())
    override val gameState: StateFlow<FlexibilityState> = _gameState.asStateFlow()

    override fun loadLevel(level: Level) {
        val config = level.config as? LevelConfig.CognitiveFlexibilityConfig ?: return
        
        val initialRule = createRule(config.ruleTypes.random())

        _gameState.update {
            it.copy(
                status = GameStatus.READY,
                level = level,
                currentRound = 0,
                currentRule = initialRule,
                roundResults = emptyList(),
                currentCombo = 0,
                maxCombo = 0,
                isStimulusVisible = false,
                switchCount = 0,
                isSwitchingRule = false,
                postSwitchTrialsLeft = 0
            )
        }
    }

    private fun createRule(type: RuleType): CognitiveRule {
        return when (type) {
            RuleType.COLOR_MATCH -> CognitiveRule("color_match", type, R.string.rule_color_match)
            RuleType.SHAPE_MATCH -> CognitiveRule("shape_match", type, R.string.rule_shape_match)
            else -> CognitiveRule("default", RuleType.COLOR_MATCH, R.string.rule_color_match)
        }
    }

    fun nextRound() {
        val state = _gameState.value
        val config = state.level?.config as? LevelConfig.CognitiveFlexibilityConfig ?: return

        if (state.currentRound >= config.roundCount) {
            _gameState.update { it.copy(status = GameStatus.WON) }
            return
        }

        // Check for rule switch
        var nextRule = state.currentRule ?: createRule(config.ruleTypes.random())
        var isSwitching = false
        var newSwitchCount = state.switchCount

        if (state.currentRound > 0 && state.currentRound % config.switchInterval == 0) {
            val availableTypes = config.ruleTypes.filter { it != state.currentRule?.type }
            if (availableTypes.isNotEmpty()) {
                nextRule = createRule(availableTypes.random())
                isSwitching = true
                newSwitchCount++
            }
        }

        val stimulus = AttentionStimulus(
            color = PuzzleColor.entries.random(),
            shape = Shape.entries.random()
        )

        // Generate 4 options including the correct one
        val expectedResponse = nextRule.getExpectedResponse(stimulus)
        val options = mutableSetOf(expectedResponse)
        
        if (nextRule.type == RuleType.COLOR_MATCH) {
            val allColors = PuzzleColor.entries.shuffled()
            for (color in allColors) {
                if (options.size >= 4) break
                options.add(color)
            }
        } else {
            val allShapes = Shape.entries.shuffled()
            for (shape in allShapes) {
                if (options.size >= 4) break
                options.add(shape)
            }
        }

        _gameState.update {
            it.copy(
                currentRound = it.currentRound + 1,
                currentStimulus = stimulus,
                currentRule = nextRule,
                isSwitchingRule = isSwitching,
                postSwitchTrialsLeft = if (isSwitching) POST_SWITCH_WINDOW else it.postSwitchTrialsLeft,
                isStimulusVisible = true,
                roundStartTime = System.currentTimeMillis(),
                status = GameStatus.PLAYING,
                switchCount = newSwitchCount,
                availableOptions = options.toList().shuffled()
            )
        }
    }

    fun onUserAction(userResponse: Any) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING || !state.isStimulusVisible) return

        val now = System.currentTimeMillis()
        val reactionTime = now - state.roundStartTime
        val stimulus = state.currentStimulus ?: return
        val rule = state.currentRule ?: return

        val expectedResponse = rule.getExpectedResponse(stimulus)
        val isCorrect = userResponse == expectedResponse
        
        val newCombo = if (isCorrect) state.currentCombo + 1 else 0

        val result = FlexibilityRoundResult(
            roundNumber = state.currentRound,
            ruleId = rule.id,
            ruleType = rule.type,
            stimulus = stimulus,
            isCorrect = isCorrect,
            reactionTimeMillis = reactionTime,
            userResponse = userResponse.toString(),
            wasAfterRuleSwitch = state.postSwitchTrialsLeft > 0,
            switchNumber = state.switchCount,
            score = 0,
            combo = newCombo
        )

        _gameState.update {
            it.copy(
                roundResults = it.roundResults + result,
                currentCombo = newCombo,
                maxCombo = maxOf(it.maxCombo, newCombo),
                isStimulusVisible = false,
                isSwitchingRule = false,
                postSwitchTrialsLeft = maxOf(0, it.postSwitchTrialsLeft - 1),
                lastFeedback = if (isCorrect) FeedbackType.CORRECT else FeedbackType.INCORRECT
            )
        }
    }

    override fun reset() {
        _gameState.update { FlexibilityState() }
    }

    override fun pause() {
        _gameState.update { it.copy(status = GameStatus.PAUSED) }
    }

    override fun resume() {
        _gameState.update { it.copy(status = GameStatus.PLAYING) }
    }
}