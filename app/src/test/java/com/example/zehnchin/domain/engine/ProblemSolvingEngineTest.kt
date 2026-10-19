package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProblemSolvingEngineTest {

    private val engine = ProblemSolvingEngine()

    @Test
    fun `test move logic and tube selection`() {
        val level = Level(
            id = 6001, chapterId = 6, levelNumber = 1, difficulty = 1,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE), emptyList()),
                tubeCapacity = 2, maxMoves = 5, optimalMoves = 1
            )
        )
        engine.loadLevel(level)
        
        // Select source
        engine.onTubeSelected(0)
        assertEquals(0, engine.gameState.value.selectedTubeId)
        
        // Select target
        engine.onTubeSelected(1)
        assertEquals(null, engine.gameState.value.selectedTubeId)
        assertEquals(1, engine.gameState.value.movesMade)
        assertEquals(4, engine.gameState.value.remainingMoves)
        
        // Verify layer moved
        assertTrue(engine.gameState.value.tubes[0].layers.size == 1)
        assertTrue(engine.gameState.value.tubes[1].layers.size == 1)
        assertEquals(PuzzleColor.BLUE, engine.gameState.value.tubes[1].topLayer?.color)
    }

    @Test
    fun `test undo reverts state`() {
        val level = Level(
            id = 6001, chapterId = 6, levelNumber = 1, difficulty = 1,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED), emptyList()),
                tubeCapacity = 2, maxMoves = 5, optimalMoves = 1
            )
        )
        engine.loadLevel(level)
        
        engine.onTubeSelected(0)
        engine.onTubeSelected(1)
        assertEquals(1, engine.gameState.value.movesMade)
        
        engine.undo()
        assertEquals(0, engine.gameState.value.movesMade)
        assertTrue(engine.gameState.value.tubes[1].isEmpty)
    }
}