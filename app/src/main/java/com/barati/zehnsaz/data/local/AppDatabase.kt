package com.barati.zehnsaz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.barati.zehnsaz.data.local.dao.AssessmentDao
import com.barati.zehnsaz.data.local.dao.AttemptDao
import com.barati.zehnsaz.data.local.dao.CognitiveSessionDao
import com.barati.zehnsaz.data.local.dao.LevelDao
import com.barati.zehnsaz.data.local.dao.PersonalBestDao
import com.barati.zehnsaz.data.local.dao.PlayerDao
import com.barati.zehnsaz.data.local.entities.*

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
        TrialRecordEntity::class,
        AssessmentMeasureEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
    abstract fun attemptDao(): AttemptDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun cognitiveSessionDao(): CognitiveSessionDao
    abstract fun personalBestDao(): PersonalBestDao
    abstract fun playerDao(): PlayerDao
}