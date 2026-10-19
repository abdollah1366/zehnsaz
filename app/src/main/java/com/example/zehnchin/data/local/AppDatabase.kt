package com.example.zehnchin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.zehnchin.data.local.dao.AttemptDao
import com.example.zehnchin.data.local.dao.CognitiveSessionDao
import com.example.zehnchin.data.local.dao.LevelDao
import com.example.zehnchin.data.local.dao.PersonalBestDao
import com.example.zehnchin.data.local.dao.PlayerDao
import com.example.zehnchin.data.local.entities.*

@Database(
    entities = [
        LevelProgressEntity::class,
        CognitiveSessionEntity::class,
        PersonalBestEntity::class,
        PlayerProfileEntity::class,
        CurrencyTransactionEntity::class,
        InventoryItemEntity::class,
        AchievementEntity::class,
        EntitlementEntity::class,
        LevelAttemptEntity::class,
        TrialRecordEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
    abstract fun attemptDao(): AttemptDao
    abstract fun cognitiveSessionDao(): CognitiveSessionDao
    abstract fun personalBestDao(): PersonalBestDao
    abstract fun playerDao(): PlayerDao
}