package com.example.zehnchin.domain.model

enum class InhibitoryStimulusType {
    GO,
    NO_GO
}

enum class InhibitoryResponseType {
    GO_CORRECT,
    GO_MISSED,
    GO_INCORRECT,
    NO_GO_CORRECT,
    NO_GO_INCORRECT
}

data class InhibitoryRoundResult(
    val roundNumber: Int,
    val stimulusType: InhibitoryStimulusType,
    val responseType: InhibitoryResponseType,
    val reactionTimeMillis: Long?,
    val isCorrect: Boolean,
    val score: Int,
    val combo: Int,
    /** The response window that was in force on this trial. */
    val responseWindowMillis: Long = 0L
)