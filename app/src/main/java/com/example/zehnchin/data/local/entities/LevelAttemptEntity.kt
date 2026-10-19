package com.example.zehnchin.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One play of one level, win or lose. Kept separate from [LevelProgressEntity],
 * which only stores the best result per level.
 */
@Entity(
    tableName = "level_attempts",
    indices = [Index("playerId"), Index("levelId")]
)
data class LevelAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: String,
    val levelId: Long,
    val chapterId: Int,
    val startTime: Long,
    val endTime: Long,
    val isWon: Boolean,
    val stars: Int,
    val score: Int,
    val accuracy: Int
)
