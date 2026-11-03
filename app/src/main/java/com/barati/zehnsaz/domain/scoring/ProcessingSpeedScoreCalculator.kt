package com.barati.zehnsaz.domain.scoring

import com.barati.zehnsaz.domain.model.ResponseType
import kotlin.math.max

object ProcessingSpeedScoreCalculator {

    data class SpeedScoreResult(
        val finalScore: Int,
        val accuracyPercent: Int,
        val avgReactionTime: Long,
        val bestReactionTime: Long,
        val maxCombo: Int,
        val stars: Int
    )

    fun calculate(
        results: List<com.barati.zehnsaz.domain.model.ProcessingRoundResult>,
        difficultyLevel: Int,
        timeLimitMillis: Long
    ): SpeedScoreResult {
        if (results.isEmpty()) return SpeedScoreResult(0, 0, 0, 0, 0, 0)

        val totalRounds = results.size
        val correctResponses = results.count { it.responseType == ResponseType.CORRECT }
        val accuracy = (correctResponses.toFloat() / totalRounds.toFloat()) * 100

        val validReactionTimes = results.filter { it.responseType == ResponseType.CORRECT && it.reactionTimeMillis > 0 }
            .map { it.reactionTimeMillis }
        
        val avgRT = if (validReactionTimes.isNotEmpty()) validReactionTimes.average().toLong() else 0L
        val bestRT = if (validReactionTimes.isNotEmpty()) validReactionTimes.minOrNull() ?: 0L else 0L
        val maxCombo = results.maxOfOrNull { it.combo } ?: 0

        // Speed Factor: normalized RT against limit.
        val speedFactor = if (avgRT > 0) 1f - (avgRT.toFloat() / timeLimitMillis.coerceAtLeast(1000L).toFloat()) else 0f
        val speedScore = (max(0f, speedFactor) * 100).toInt()

        // Formula: (Accuracy * 10) + (Speed * 10) + (MaxCombo * 20) * Difficulty
        val baseScore = (accuracy * 10) + (speedScore * 10) + (maxCombo * 20)
        val finalScore = (baseScore * (1 + (difficultyLevel * 0.1f))).toInt()

        val stars = when {
            accuracy >= 90 && avgRT < timeLimitMillis * 0.4 -> 3
            accuracy >= 75 && avgRT < timeLimitMillis * 0.7 -> 2
            accuracy >= 60 -> 1
            else -> 0
        }

        return SpeedScoreResult(
            finalScore = finalScore,
            accuracyPercent = accuracy.toInt(),
            avgReactionTime = avgRT,
            bestReactionTime = bestRT,
            maxCombo = maxCombo,
            stars = stars
        )
    }
}