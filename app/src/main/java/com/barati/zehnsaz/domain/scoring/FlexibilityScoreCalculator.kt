package com.barati.zehnsaz.domain.scoring

import com.barati.zehnsaz.domain.model.FlexibilityRoundResult
import kotlin.math.max

object FlexibilityScoreCalculator {

    data class FlexibilityScoreResult(
        val finalScore: Int,
        val accuracyPercent: Int,
        val postSwitchAccuracy: Int,
        val switchErrorRate: Int,
        val avgReactionTime: Long,
        val switchCostMillis: Long,
        val stars: Int
    )

    fun calculate(
        results: List<FlexibilityRoundResult>,
        difficultyLevel: Int,
        timeLimitMillis: Long
    ): FlexibilityScoreResult {
        if (results.isEmpty()) return FlexibilityScoreResult(0, 0, 0, 0, 0, 0, 0)

        val totalRounds = results.size
        val correctResponses = results.count { it.isCorrect }
        val accuracy = (correctResponses.toFloat() / totalRounds * 100).toInt()

        val postSwitchTrials = results.filter { it.wasAfterRuleSwitch }
        val postSwitchCorrect = postSwitchTrials.count { it.isCorrect }
        val postSwitchAccuracy = if (postSwitchTrials.isNotEmpty()) (postSwitchCorrect.toFloat() / postSwitchTrials.size * 100).toInt() else 0
        
        val switchErrors = postSwitchTrials.count { !it.isCorrect }
        val switchErrorRate = if (postSwitchTrials.isNotEmpty()) (switchErrors.toFloat() / postSwitchTrials.size * 100).toInt() else 0

        val avgRT = if (results.isNotEmpty()) results.map { it.reactionTimeMillis }.average().toLong() else 0L
        
        val rtBeforeSwitch = results.filter { !it.wasAfterRuleSwitch }.map { it.reactionTimeMillis }
        val rtAfterSwitch = results.filter { it.wasAfterRuleSwitch }.map { it.reactionTimeMillis }
        
        val avgRTBefore = if (rtBeforeSwitch.isNotEmpty()) rtBeforeSwitch.average() else 0.0
        val avgRTAfter = if (rtAfterSwitch.isNotEmpty()) rtAfterSwitch.average() else 0.0
        val switchCost = (avgRTAfter - avgRTBefore).toLong()

        // Formula: (Acc * 10) + (PostSwitchAcc * 15) - (SwitchErrorRate * 5) + (Difficulty * 50)
        val baseScore = (accuracy * 10) + (postSwitchAccuracy * 15) - (switchErrorRate * 5)
        val finalScore = (maxOf(0, baseScore) * (1 + (difficultyLevel * 0.15f))).toInt()

        val stars = when {
            accuracy >= 90 && postSwitchAccuracy >= 80 -> 3
            accuracy >= 75 && postSwitchAccuracy >= 60 -> 2
            accuracy >= 50 -> 1
            else -> 0
        }

        return FlexibilityScoreResult(
            finalScore = finalScore,
            accuracyPercent = accuracy,
            postSwitchAccuracy = postSwitchAccuracy,
            switchErrorRate = switchErrorRate,
            avgReactionTime = avgRT,
            switchCostMillis = switchCost,
            stars = stars
        )
    }
}