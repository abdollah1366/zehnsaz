package com.example.zehnchin.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val playerId: String = "default_player",
    val level: Int = 1,
    val xp: Int = 0,
    val totalCoins: Int = 0,
    val totalGems: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoginDate: Long = 0,
    val completedLevels: Int = 0,
    val completedChapters: Int = 0,
    val totalStars: Int = 0
)