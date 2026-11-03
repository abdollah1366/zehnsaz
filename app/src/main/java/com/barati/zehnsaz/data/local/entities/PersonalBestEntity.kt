package com.barati.zehnsaz.data.local.entities

import androidx.room.Entity

@Entity(tableName = "personal_bests", primaryKeys = ["playerId", "cognitiveDomain"])
data class PersonalBestEntity(
    val playerId: String,
    val cognitiveDomain: String,
    val bestScore: Int = 0,
    val fastestReactionTime: Long = Long.MAX_VALUE,
    val highestCombo: Int = 0,
    val bestAccuracy: Int = 0,
    val lowestFalseAlarmRate: Int = 100,
    val bestSwitchAccuracy: Int = 0
)