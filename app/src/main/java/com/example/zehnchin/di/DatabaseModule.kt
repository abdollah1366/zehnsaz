package com.example.zehnchin.di

import android.content.Context
import androidx.room.Room
import com.example.zehnchin.data.local.AppDatabase
import com.example.zehnchin.data.local.dao.CognitiveSessionDao
import com.example.zehnchin.data.local.dao.LevelDao
import com.example.zehnchin.data.local.dao.PersonalBestDao
import com.example.zehnchin.data.local.dao.PlayerDao
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
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideLevelDao(database: AppDatabase): LevelDao = database.levelDao()

    @Provides
    fun provideCognitiveSessionDao(database: AppDatabase): CognitiveSessionDao = database.cognitiveSessionDao()

    @Provides
    fun providePersonalBestDao(database: AppDatabase): PersonalBestDao = database.personalBestDao()

    @Provides
    fun providePlayerDao(database: AppDatabase): PlayerDao = database.playerDao()
}