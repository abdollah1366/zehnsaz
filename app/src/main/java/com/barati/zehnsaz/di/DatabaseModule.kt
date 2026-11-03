package com.barati.zehnsaz.di

import android.content.Context
import androidx.room.Room
import com.barati.zehnsaz.data.local.AppDatabase
import com.barati.zehnsaz.data.local.MIGRATION_3_4
import com.barati.zehnsaz.data.local.MIGRATION_4_5
import com.barati.zehnsaz.data.local.dao.AssessmentDao
import com.barati.zehnsaz.data.local.dao.AttemptDao
import com.barati.zehnsaz.data.local.dao.CognitiveSessionDao
import com.barati.zehnsaz.data.local.dao.LevelDao
import com.barati.zehnsaz.data.local.dao.PersonalBestDao
import com.barati.zehnsaz.data.local.dao.PlayerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "zehnchin_db"
        )
            // No destructive fallback: a missing migration should fail loudly, not erase player data.
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    @Provides
    fun provideLevelDao(database: AppDatabase): LevelDao = database.levelDao()

    @Provides
    fun provideAttemptDao(database: AppDatabase): AttemptDao = database.attemptDao()

    @Provides
    fun provideAssessmentDao(database: AppDatabase): AssessmentDao = database.assessmentDao()

    @Provides
    fun provideCognitiveSessionDao(database: AppDatabase): CognitiveSessionDao = database.cognitiveSessionDao()

    @Provides
    fun providePersonalBestDao(database: AppDatabase): PersonalBestDao = database.personalBestDao()

    @Provides
    fun providePlayerDao(database: AppDatabase): PlayerDao = database.playerDao()
}