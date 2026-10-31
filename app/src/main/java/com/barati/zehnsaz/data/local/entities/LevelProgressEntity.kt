package com.barati.zehnsaz.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Long,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0,
    val stars: Int = 0,
    val bestTimeSeconds: Long = 0,
    val bestMoves: Int = 0
)