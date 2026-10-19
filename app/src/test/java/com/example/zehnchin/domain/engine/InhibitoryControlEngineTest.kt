package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InhibitoryControlEngineTest {

    private val engine = InhibitoryControlEngine()

    @Test
    fun `test initial load state`() {
        val level = Level(
            id = 4001,
            chapterId = 4,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.InhibitoryControlConfig(
                roundCount = 10,
                goProbability = 0.7f,
                noGoProbability = 0.3f,
                stimulusDuration = 1000L,
                responseWindow = 1500L
            )
        )
        engine.loadLevel(level)
        
        val state = engine.gameState.value
        assertEquals(GameStatus.READY, state.status)
        assertEquals(0, state.currentRound)
    }

    @Test
    fun `test no-go correct when no response is taken`() {
        val level = Level(
            id = 4001,
            chapterId = 4,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.InhibitoryControlConfig(
                roundCount = 1,
                goProbability = 0.0f, // Force No-Go
                noGoProbability = 1.0f,
                stimulusDuration = 1000L,
                responseWindow = 1500L
            )
        )
        engine.loadLevel(level)
        engine.nextRound()
        
        assertEquals(InhibitoryStimulusType.NO_GO, engine.gameState.value.stimulusType)
        
        // Simulate no user action
        engine.onUserAction(clicked = false)
        
        val lastResult = engine.gameState.value.roundResults.last()
        assertEquals(InhibitoryResponseType.NO_GO_CORRECT, lastResult.responseType)
        assertTrue(lastResult.isCorrect)
    }

    @Test
    fun `test no-go false alarm when user clicks`() {
        val level = Level(
            id = 4001,
            chapterId = 4,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.InhibitoryControlConfig(
                roundCount = 1,
                goProbability = 0.0f,
                noGoProbability = 1.0f,
                stimulusDuration = 1000L,
                responseWindow = 1500L
            )
        )
        engine.loadLevel(level)
        engine.nextRound()
        
        engine.onUserAction(clicked = true)
        
        val lastResult = engine.gameState.value.roundResults.last()
        assertEquals(InhibitoryResponseType.NO_GO_INCORRECT, lastResult.responseType)
        assertFalse(lastResult.isCorrect)
        assertEquals(0, engine.gameState.value.currentCombo)
    }
}