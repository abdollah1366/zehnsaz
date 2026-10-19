package com.example.zehnchin.domain.engine

import kotlin.math.roundToLong

/**
 * Weighted up/down staircase for the Go response window.
 *
 * After a Go hit the window shrinks by step × (1 − target); after a Go miss it grows by step × target.
 * The long-run hit rate settles where the expected change is zero, which is P(hit) = target.
 * This is the standard weighting for a non-50% target. Standard 1-up/1-down steps settle at 50%.
 */
data class ResponseWindowStaircase(
    val windowMillis: Long,
    val targetHitRate: Double = 0.8,
    val stepMillis: Long = 50L,
    val minMillis: Long = 250L,
    val maxMillis: Long = 2000L
) {
    fun afterGoTrial(hit: Boolean): ResponseWindowStaircase {
        val delta = if (hit) -stepMillis * (1.0 - targetHitRate) else stepMillis * targetHitRate
        val next = (windowMillis + delta).roundToLong().coerceIn(minMillis, maxMillis)
        return copy(windowMillis = next)
    }
}
