package com.example.zehnchin.domain.model

enum class ResponseType {
    CORRECT,
    INCORRECT,
    TIMEOUT
}

data class ProcessingRoundResult(
    val roundNumber: Int,
    val stimulus: AttentionStimulus,
    val responseType: ResponseType,
    val reactionTimeMillis: Long,
    val score: Int,
    val combo: Int
)