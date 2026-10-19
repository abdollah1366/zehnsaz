package com.example.zehnchin.domain.scoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttentionScoreCalculatorTest {

    @Test
    fun `test perfect accuracy and fast reaction time gives 3 stars`() {
        val result = AttentionScoreCalculator.calculate(
            correctAnswers = 10,
            totalAttempts = 10,
            totalReactionTimeMillis = 2000L, // 200ms avg
            difficultyLevel = 5,
            timeLimitMillis = 10000L
        )
        
        assertEquals(100, result.accuracyPercent)
        assertEquals(3, result.stars)
        assertTrue(result.finalScore > 1500)
    }

    @Test
    fun `test low accuracy gives 0 stars`() {
        val result = AttentionScoreCalculator.calculate(
            correctAnswers = 5,
            totalAttempts = 10,
            totalReactionTimeMillis = 5000L,
            difficultyLevel = 1,
            timeLimitMillis = 10000L
        )
        
        assertEquals(50, result.accuracyPercent)
        assertEquals(0, result.stars)
    }
}