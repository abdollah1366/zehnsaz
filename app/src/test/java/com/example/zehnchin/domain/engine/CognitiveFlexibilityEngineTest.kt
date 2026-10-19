package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import com.example.zehnchin.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CognitiveFlexibilityEngineTest {

    private val engine = CognitiveFlexibilityEngine()

    @Test
    fun `test rule switching after defined interval`() {
        val level = Level(
            id = 5001,
            chapterId = 5,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.CognitiveFlexibilityConfig(
                roundCount = 10,
                ruleTypes = listOf(RuleType.COLOR_MATCH, RuleType.SHAPE_MATCH),
                switchInterval = 2,
                responseTimeLimit = 3000L
            )
        )
        engine.loadLevel(level)
        
        val initialRule = engine.gameState.value.currentRule
        
        // Round 1
        engine.nextRound()
        assertEquals(initialRule, engine.gameState.value.currentRule)
        engine.onUserAction("ANY")

        // Round 2 - Still same rule
        engine.nextRound()
        assertEquals(initialRule, engine.gameState.value.currentRule)
        engine.onUserAction("ANY")

        // Round 3 - Rule should switch
        engine.nextRound()
        assertNotEquals(initialRule?.type, engine.gameState.value.currentRule?.type)
        assertTrue(engine.gameState.value.isSwitchingRule)
        assertEquals(1, engine.gameState.value.switchCount)
    }

    @Test
    fun `the first three trials after a switch are marked as post-switch`() {
        val level = Level(
            id = 5002,
            chapterId = 5,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.CognitiveFlexibilityConfig(
                roundCount = 8,
                ruleTypes = listOf(RuleType.COLOR_MATCH, RuleType.SHAPE_MATCH),
                switchInterval = 4,
                responseTimeLimit = 3000L
            )
        )
        engine.loadLevel(level)

        // The switch happens before round 5, so rounds 5, 6 and 7 are post-switch.
        repeat(8) {
            engine.nextRound()
            engine.onUserAction("ANY")
        }

        val flags = engine.gameState.value.roundResults.map { it.wasAfterRuleSwitch }
        assertEquals(listOf(false, false, false, false, true, true, true, false), flags)
    }

    @Test
    fun `test correct response evaluation based on current rule`() {
        val rule = CognitiveRule("test", RuleType.COLOR_MATCH, R.string.rule_color_match)
        val stimulus = AttentionStimulus(color = PuzzleColor.RED, shape = Shape.CIRCLE)
        
        assertEquals(PuzzleColor.RED, rule.getExpectedResponse(stimulus))
        
        val shapeRule = CognitiveRule("test2", RuleType.SHAPE_MATCH, R.string.rule_shape_match)
        assertEquals(Shape.CIRCLE, shapeRule.getExpectedResponse(stimulus))
    }
}