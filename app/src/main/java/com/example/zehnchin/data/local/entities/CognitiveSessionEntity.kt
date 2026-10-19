package com.example.zehnchin.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cognitive_sessions")
data class CognitiveSessionEntity(
    @PrimaryKey val id: String,
    val playerId: String,
    val chapterId: Int,
    val startTime: Long,
    val endTime: Long,
    val levelsCompleted: Int,
    val accuracy: Int,
    val averageReactionTime: Long,
    val mistakes: Int,
    val highestDifficulty: Int,
    val score: Int,
    
    // Domain Specific
    val goTrials: Int = 0,
    val noGoTrials: Int = 0,
    val noGoCorrect: Int = 0,
    val falseAlarms: Int = 0,
    val falseAlarmRate: Int = 0,
    
    // Flexibility
    val switchCount: Int = 0,
    val switchErrors: Int = 0,
    val preSwitchAccuracy: Int = 0,
    val postSwitchAccuracy: Int = 0,

    // Problem Solving
    val averageEfficiency: Int = 0,
    val totalMoves: Int = 0,
    val totalOptimalMoves: Int = 0,
    val hintUsage: Int = 0,
    val deadlocks: Int = 0
)