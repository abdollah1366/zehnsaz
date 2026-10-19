package com.example.zehnchin.domain.scoring

import kotlin.math.max

object WorkingMemoryScoreCalculator {

    data class MemoryScoreResult(
        val finalScore: Int,
        val accuracyPercent: Int,
        val recallSpeedScore: Int,
        val stars: Int,
        val maxSequenceLength: Int
    )

    fun calculate(
        correctItems: Int,
        totalItems: Int,
        recallTimeMillis: Long,
        difficultyLevel: Int,
        timeLimitMillis: Long,
        sequenceLength: Int
    ): MemoryScoreResult {
        if (totalItems == 0) return MemoryScoreResult(0, 0, 0, 0, sequenceLength)

        val accuracy = (correctItems.toFloat() / totalItems.toFloat()) * 100
        val speedFactor = 1f - (recallTimeMillis.toFloat() / timeLimitMillis.coerceAtLeast(1000L).toFloat())
        val recallSpeedScore = (max(0f, speedFactor) * 100).toInt()

        // Formula: (Accuracy * 15) + (Speed * 5) + (SequenceLength * 50) * Difficulty
        val baseScore = (accuracy * 15) + (recallSpeedScore * 5) + (sequenceLength * 50)
        val finalScore = (baseScore * (1 + (difficultyLevel * 0.15f))).toInt()

        val stars = when {
            accuracy >= 100 && recallTimeMillis < timeLimitMillis * 0.4 -> 3
            accuracy >= 80 && recallTimeMillis < timeLimitMillis * 0.7 -> 2
            accuracy >= 60 -> 1
            else -> 0
        }

        return MemoryScoreResult(
            finalScore = finalScore,
            accuracyPercent = accuracy.toInt(),
            recallSpeedScore = recallSpeedScore,
            stars = stars,
            maxSequenceLength = sequenceLength
        )
    }
}