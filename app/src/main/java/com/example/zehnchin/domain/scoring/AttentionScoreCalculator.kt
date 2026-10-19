package com.example.zehnchin.domain.scoring

import kotlin.math.max

object AttentionScoreCalculator {
    
    data class ScoreResult(
        val finalScore: Int,
        val accuracyPercent: Int,
        val reactionSpeedScore: Int,
        val stars: Int
    )

    fun calculate(
        correctAnswers: Int,
        totalAttempts: Int,
        totalReactionTimeMillis: Long,
        difficultyLevel: Int,
        timeLimitMillis: Long
    ): ScoreResult {
        if (totalAttempts == 0) return ScoreResult(0, 0, 0, 0)

        val accuracy = (correctAnswers.toFloat() / totalAttempts.toFloat()) * 100
        val avgReactionTime = totalReactionTimeMillis.toFloat() / max(1, correctAnswers).toFloat()
        
        // Speed score: Higher is better. Normalized against time limit.
        // If avgRT is 20% of limit, speedScore is high.
        val speedFactor = 1f - (avgReactionTime / timeLimitMillis.toFloat().coerceAtLeast(1000f))
        val reactionSpeedScore = (max(0f, speedFactor) * 100).toInt()

        // Final score formula: (Accuracy * 10) + (Speed * 5) * Difficulty
        val baseScore = (accuracy * 10) + (reactionSpeedScore * 5)
        val finalScore = (baseScore * (1 + (difficultyLevel * 0.1f))).toInt()

        val stars = when {
            accuracy >= 95 && avgReactionTime < timeLimitMillis.toFloat() * 0.3f -> 3
            accuracy >= 80 && avgReactionTime < timeLimitMillis.toFloat() * 0.6f -> 2
            accuracy >= 60 -> 1
            else -> 0
        }

        return ScoreResult(
            finalScore = finalScore,
            accuracyPercent = accuracy.toInt(),
            reactionSpeedScore = reactionSpeedScore,
            stars = stars
        )
    }
}