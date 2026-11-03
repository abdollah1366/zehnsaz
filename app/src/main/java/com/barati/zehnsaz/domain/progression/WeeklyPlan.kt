package com.barati.zehnsaz.domain.progression

import com.barati.zehnsaz.data.local.entities.LevelAttemptEntity
import java.util.Calendar

/** One planned session: a chapter, and whether it was played this week. */
data class PlannedSession(val chapterId: Int, val done: Boolean)

data class WeeklyPlan(val sessions: List<PlannedSession>) {
    val completedCount: Int get() = sessions.count { it.done }
}

/**
 * Picks the week's sessions from the chapters where the player is least accurate, weakest first.
 * A chapter never played counts as the weakest. Ranking uses only attempts from before the week
 * started, so the plan does not reshuffle while the player is training.
 */
object WeeklyPlanner {

    const val SESSIONS_PER_WEEK = 3

    /** Chapter 0 is the tutorial and is not part of the plan. */
    private val trainableChapters = 1..6

    fun build(attempts: List<LevelAttemptEntity>, weekStartMillis: Long): WeeklyPlan {
        val earlier = attempts.filter { it.startTime < weekStartMillis }
        val playedThisWeek = attempts
            .filter { it.startTime >= weekStartMillis }
            .map { it.chapterId }
            .toSet()

        val weakest = trainableChapters
            .sortedWith(compareBy<Int> { averageAccuracy(earlier, it) ?: NEVER_PLAYED }.thenBy { it })
            .take(SESSIONS_PER_WEEK)

        return WeeklyPlan(weakest.map { PlannedSession(it, done = it in playedThisWeek) })
    }

    /** Midnight at the start of the week that contains [nowMillis]. Weeks begin on [firstDayOfWeek]. */
    fun weekStart(nowMillis: Long, firstDayOfWeek: Int = Calendar.getInstance().firstDayOfWeek): Long =
        Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

    private fun averageAccuracy(attempts: List<LevelAttemptEntity>, chapterId: Int): Double? =
        attempts
            .filter { it.chapterId == chapterId }
            .map { it.accuracy.toDouble() }
            .average()
            .takeUnless { it.isNaN() }

    private const val NEVER_PLAYED = -1.0
}
