package com.barati.zehnsaz.domain.model

data class CognitiveSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val playerId: String = "default_player",
    val chapterId: Int,
    val startTime: Long,
    val endTime: Long = 0,
    val levelsCompleted: Int = 0,
    val accuracy: Int = 0,
    val averageReactionTime: Long = 0,
    val mistakes: Int = 0,
    val highestDifficulty: Int = 1,
    val score: Int = 0
)