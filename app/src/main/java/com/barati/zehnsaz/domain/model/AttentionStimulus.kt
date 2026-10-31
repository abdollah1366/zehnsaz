package com.barati.zehnsaz.domain.model

import com.barati.zehnsaz.domain.engine.WorkingMemoryEngine
import java.util.UUID

data class AttentionStimulus(
    val id: String = UUID.randomUUID().toString(),
    val color: PuzzleColor,
    val shape: Shape,
    val isTarget: Boolean = false,
    val scale: Float = 1.0f
) {
    /**
     * Checks if this stimulus matches the target criteria.
     */
    fun matches(targetColor: PuzzleColor, targetShape: Shape): Boolean {
        return color == targetColor && shape == targetShape
    }

    /**
     * Checks if this stimulus matches another one based on specific memory rules.
     */
    fun matchesRule(other: AttentionStimulus, rule: WorkingMemoryEngine.MemoryRule): Boolean {
        return when (rule) {
            WorkingMemoryEngine.MemoryRule.SHAPE_ONLY -> shape == other.shape
            WorkingMemoryEngine.MemoryRule.SHAPE_COLOR -> shape == other.shape && color == other.color
            WorkingMemoryEngine.MemoryRule.SHAPE_COLOR_SIZE,
            WorkingMemoryEngine.MemoryRule.ALL -> shape == other.shape && color == other.color && Math.abs(scale - other.scale) < 0.01f
        }
    }
}
