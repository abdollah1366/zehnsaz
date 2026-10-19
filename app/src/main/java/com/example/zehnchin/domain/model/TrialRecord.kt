package com.example.zehnchin.domain.model

/**
 * One trial (stimulus → response) inside a level attempt.
 * Labels are game-specific and stored as plain text so that every game can share one table.
 */
data class TrialRecord(
    val trialIndex: Int,
    val stimulusLabel: String,
    val responseLabel: String,
    val isCorrect: Boolean,
    val reactionTimeMillis: Long?
)
