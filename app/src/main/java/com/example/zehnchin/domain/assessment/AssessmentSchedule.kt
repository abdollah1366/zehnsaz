package com.example.zehnchin.domain.assessment

/**
 * When the baseline battery is due again. Repeating it on a fixed interval is what turns a
 * single score into a measure of change over time.
 */
object AssessmentSchedule {

    const val DEFAULT_INTERVAL_DAYS = 14

    private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

    /** True when the battery was never taken, or at least [intervalDays] have passed since the last time. */
    fun isDue(
        nowMillis: Long,
        lastCompletedMillis: Long?,
        intervalDays: Int = DEFAULT_INTERVAL_DAYS
    ): Boolean {
        if (lastCompletedMillis == null) return true
        return nowMillis - lastCompletedMillis >= intervalDays * MILLIS_PER_DAY
    }
}
