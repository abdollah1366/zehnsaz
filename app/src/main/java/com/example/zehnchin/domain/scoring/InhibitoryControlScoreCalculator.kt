package com.example.zehnchin.domain.scoring

import com.example.zehnchin.domain.model.InhibitoryResponseType
import com.example.zehnchin.domain.model.InhibitoryRoundResult

object InhibitoryControlScoreCalculator {

    data class InhibitionScoreResult(
        val finalScore: Int,
        val goAccuracy: Int,
        val noGoAccuracy: Int,
        val falseAlarmRate: Int,
        val avgReactionTime: Long,
        val stars: Int
    )

    fun calculate(
        results: List<InhibitoryRoundResult>,
        difficultyLevel: Int,
        responseWindowMillis: Long
    ): InhibitionScoreResult {
        if (results.isEmpty()) return InhibitionScoreResult(0, 0, 0, 0, 0, 0)

        val goTrials = results.filter { it.stimulusType == com.example.zehnchin.domain.model.InhibitoryStimulusType.GO }
        val noGoTrials = results.filter { it.stimulusType == com.example.zehnchin.domain.model.InhibitoryStimulusType.NO_GO }

        val goCorrect = goTrials.count { it.responseType == InhibitoryResponseType.GO_CORRECT }
        val noGoCorrect = noGoTrials.count { it.responseType == InhibitoryResponseType.NO_GO_CORRECT }
        val falseAlarms = noGoTrials.count { it.responseType == InhibitoryResponseType.NO_GO_INCORRECT }

        val goAccuracy = if (goTrials.isNotEmpty()) (goCorrect.toFloat() / goTrials.size * 100).toInt() else 0
        val noGoAccuracy = if (noGoTrials.isNotEmpty()) (noGoCorrect.toFloat() / noGoTrials.size * 100).toInt() else 0
        val falseAlarmRate = if (noGoTrials.isNotEmpty()) (falseAlarms.toFloat() / noGoTrials.size * 100).toInt() else 0

        val validGoRTs = goTrials.filter { it.responseType == InhibitoryResponseType.GO_CORRECT }
            .mapNotNull { it.reactionTimeMillis }
        
        val avgRT = if (validGoRTs.isNotEmpty()) validGoRTs.average().toLong() else 0L

        // Speed factor for Go trials
        val speedFactor = if (avgRT > 0) 1f - (avgRT.toFloat() / responseWindowMillis.coerceAtLeast(1000L).toFloat()) else 0f
        
        // Base Score: (GoAcc * 5) + (NoGoAcc * 15) + (Speed * 10) - (FalseAlarms * 20)
        // Weight NoGo higher as it's the core of this chapter
        val baseScore = (goAccuracy * 5) + (noGoAccuracy * 15) + (speedFactor * 1000).toInt() - (falseAlarms * 50)
        val finalScore = (maxOf(0, baseScore) * (1 + (difficultyLevel * 0.1f))).toInt()

        val stars = when {
            noGoAccuracy >= 90 && goAccuracy >= 80 && avgRT < responseWindowMillis * 0.5 -> 3
            noGoAccuracy >= 70 && goAccuracy >= 60 -> 2
            noGoAccuracy >= 50 -> 1
            else -> 0
        }

        return InhibitionScoreResult(
            finalScore = finalScore,
            goAccuracy = goAccuracy,
            noGoAccuracy = noGoAccuracy,
            falseAlarmRate = falseAlarmRate,
            avgReactionTime = avgRT,
            stars = stars
        )
    }
}