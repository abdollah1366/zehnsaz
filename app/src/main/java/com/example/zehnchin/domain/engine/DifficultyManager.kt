package com.example.zehnchin.domain.engine

import kotlin.math.max
import kotlin.math.min

object DifficultyManager {

    /**
     * Calculates the new difficulty level based on performance.
     * Changes are capped at +/- 1 level per round.
     */
    fun calculateAdaptiveDifficulty(
        currentDifficulty: Int,
        accuracy: Int,
        avgReactionTimeMillis: Long,
        targetReactionTimeMillis: Long
    ): Int {
        var newDifficulty = currentDifficulty

        when {
            accuracy >= 90 && avgReactionTimeMillis < targetReactionTimeMillis * 0.7 -> {
                newDifficulty += 1
            }
            accuracy < 60 || avgReactionTimeMillis > targetReactionTimeMillis -> {
                newDifficulty -= 1
            }
        }

        return newDifficulty.coerceIn(1, 10)
    }
}