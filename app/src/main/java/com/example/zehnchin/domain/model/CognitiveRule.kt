package com.example.zehnchin.domain.model

import androidx.annotation.StringRes

data class CognitiveRule(
    val id: String,
    val type: RuleType,
    @StringRes val displayTextRes: Int,
    val icon: String? = null
) {
    /**
     * Determines the correct response for a given stimulus based on this rule.
     * For simplicity, let's say the response is either matching the color or the shape.
     */
    fun getExpectedResponse(stimulus: AttentionStimulus): Any {
        return when (type) {
            RuleType.COLOR_MATCH -> stimulus.color
            RuleType.SHAPE_MATCH -> stimulus.shape
            else -> stimulus.color // Fallback
        }
    }
}