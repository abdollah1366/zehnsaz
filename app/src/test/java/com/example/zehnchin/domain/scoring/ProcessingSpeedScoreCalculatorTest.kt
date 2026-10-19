package com.example.zehnchin.domain.scoring

import com.example.zehnchin.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessingSpeedScoreCalculatorTest {

    @Test
    fun `test high accuracy and fast RT results in 3 stars`() {
        val results = listOf(
            ProcessingRoundResult(1, AttentionStimulus(color = PuzzleColor.RED, shape = Shape.CIRCLE), ResponseType.CORRECT, 400L, 0, 1),
            ProcessingRoundResult(2, AttentionStimulus(color = PuzzleColor.RED, shape = Shape.CIRCLE), ResponseType.CORRECT, 350L, 0, 2)
        )
        
        val scoreResult = ProcessingSpeedScoreCalculator.calculate(
            results = results,
            difficultyLevel = 5,
            timeLimitMillis = 2000L
        )
        
        assertEquals(100, scoreResult.accuracyPercent)
        assertEquals(3, scoreResult.stars)
        assertEquals(350L, scoreResult.bestReactionTime)
    }

    @Test
    fun `test many mistakes result in 0 stars`() {
        val results = listOf(
            ProcessingRoundResult(1, AttentionStimulus(color = PuzzleColor.RED, shape = Shape.CIRCLE), ResponseType.INCORRECT, 800L, 0, 0),
            ProcessingRoundResult(2, AttentionStimulus(color = PuzzleColor.RED, shape = Shape.CIRCLE), ResponseType.TIMEOUT, 0L, 0, 0)
        )
        
        val scoreResult = ProcessingSpeedScoreCalculator.calculate(
            results = results,
            difficultyLevel = 1,
            timeLimitMillis = 2000L
        )
        
        assertEquals(0, scoreResult.accuracyPercent)
        assertEquals(0, scoreResult.stars)
    }
}