package com.barati.zehnsaz.domain.model

data class CognitiveScoreResult(
    val finalScore: Int,
    val accuracy: Int,
    val speed: Int,
    val stars: Int
)
