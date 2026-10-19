package com.example.zehnchin.domain.scoring

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkingMemoryScoreCalculatorTest {

    @Test
    fun `test perfect recall gives 3 stars`() {
        val result = WorkingMemoryScoreCalculator.calculate(
            correctItems = 5,
            totalItems = 5,
            recallTimeMillis = 2000L,
            difficultyLevel = 5,
            timeLimitMillis = 10000L,
            sequenceLength = 5
        )
        
        assertEquals(100, result.accuracyPercent)
        assertEquals(3, result.stars)
    }

    @Test
    fun `test failing to recall all items gives fewer stars`() {
        val result = WorkingMemoryScoreCalculator.calculate(
            correctItems = 3,
            totalItems = 5,
            recallTimeMillis = 5000L,
            difficultyLevel = 1,
            timeLimitMillis = 10000L,
            sequenceLength = 5
        )
        
        assertEquals(60, result.accuracyPercent)
        assertEquals(1, result.stars)
    }
}