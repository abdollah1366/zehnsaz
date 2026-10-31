package com.barati.zehnsaz.domain.progression

import com.barati.zehnsaz.data.local.entities.LevelAttemptEntity
import kotlin.math.roundToInt

data class DomainSummary(
    val chapterId: Int,
    val attemptCount: Int,
    val bestScore: Int,
    val averageAccuracy: Int,
    val bestStars: Int
)

object AttemptSummary {

    /** Groups stored attempts by chapter (cognitive domain) and summarises each group. */
    fun summarize(attempts: List<LevelAttemptEntity>): List<DomainSummary> {
        return attempts
            .groupBy { it.chapterId }
            .map { (chapterId, group) ->
                DomainSummary(
                    chapterId = chapterId,
                    attemptCount = group.size,
                    bestScore = group.maxOf { it.score },
                    averageAccuracy = group.map { it.accuracy }.average().roundToInt(),
                    bestStars = group.maxOf { it.stars }
                )
            }
            .sortedBy { it.chapterId }
    }
}
