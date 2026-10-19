package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkingMemoryEngineTest {

    private val engine = WorkingMemoryEngine()

    @Test
    fun `test encoding phase transitions to recall`() {
        val level = Level(
            id = 2001,
            chapterId = 2,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.WorkingMemoryConfig(
                memoryLength = 3,
                encodingDuration = 3000L,
                recallTimeLimit = 15000L,
                numberOfChoices = 6
            )
        )
        engine.loadLevel(level)
        
        assertEquals(WorkingMemoryEngine.MemoryPhase.ENCODING, engine.gameState.value.phase)
        
        engine.startRecall()
        assertEquals(WorkingMemoryEngine.MemoryPhase.RECALL, engine.gameState.value.phase)
        assertTrue(engine.gameState.value.recallStartTime > 0)
    }

    @Test
    fun `test correct sequence submission results in win`() {
        val level = Level(
            id = 2001,
            chapterId = 2,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.WorkingMemoryConfig(
                memoryLength = 2,
                encodingDuration = 3000L,
                recallTimeLimit = 15000L,
                numberOfChoices = 4
            )
        )
        engine.loadLevel(level)
        val target = engine.gameState.value.targetSequence
        
        engine.startRecall()
        target.forEach { engine.onOptionSelected(it) }
        
        engine.submitAnswer()
        
        assertEquals(GameStatus.WON, engine.gameState.value.status)
    }

    @Test
    fun `test retry of the same level does not reuse the same sequence`() {
        val level = Level(
            id = 2001,
            chapterId = 2,
            levelNumber = 1,
            difficulty = 1,
            config = LevelConfig.WorkingMemoryConfig(
                memoryLength = 4,
                encodingDuration = 3000L,
                recallTimeLimit = 15000L,
                numberOfChoices = 6
            )
        )
        engine.loadLevel(level)
        val first = engine.gameState.value.targetSequence.map { it.color to it.shape }

        // Sequences of 4 items from 8 colours x 6 shapes: a repeat by chance is practically impossible.
        var differs = false
        repeat(5) {
            engine.loadLevel(level)
            if (engine.gameState.value.targetSequence.map { it.color to it.shape } != first) differs = true
        }
        assertTrue(differs)
    }
}
