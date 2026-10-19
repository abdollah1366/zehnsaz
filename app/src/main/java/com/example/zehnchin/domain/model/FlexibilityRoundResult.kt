package com.example.zehnchin.domain.model

data class FlexibilityRoundResult(
    val roundNumber: Int,
    val ruleId: String,
    val ruleType: RuleType,
    val stimulus: AttentionStimulus,
    val isCorrect: Boolean,
    val reactionTimeMillis: Long,
    val userResponse: String,
    val wasAfterRuleSwitch: Boolean,
    val switchNumber: Int,
    val score: Int,
    val combo: Int
)