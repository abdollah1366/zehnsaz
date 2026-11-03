package com.barati.zehnsaz.domain.engine

import com.barati.zehnsaz.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessingSpeedEngineTest {

    private val engine = ProcessingSpeedEngine()

    @Test
    fun `test initial load state`() {
        val level = Level(
            id = 3001,
            chapterId = 3,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.ProcessingSpeedConfig(
                roundCount = 10,
                stimulusDuration = 1000L,
                responseTimeLimit = 2000L
            )
        )
        engine.loadLevel(level)
        
        val state = engine.gameState.value
        assertEquals(GameStatus.READY, state.status)
        assertEquals(0, state.currentRound)
        assertTrue(state.targetCriteria != null)
    }

    @Test
    fun `test correct action increases combo`() {
        val level = Level(
            id = 3001,
            chapterId = 3,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.ProcessingSpeedConfig(
                roundCount = 10,
                stimulusDuration = 1000L,
                responseTimeLimit = 2000L,
                targetProbability = 1.0f // Force target
            )
        )
        engine.loadLevel(level)
        engine.startTrial()
        
        assertTrue(engine.gameState.value.isStimulusVisible)
        
        engine.onAction(responded = true)
        
        assertEquals(1, engine.gameState.value.currentCombo)
        assertFalse(engine.gameState.value.isStimulusVisible)
    }
}