package com.example.zehnchin.domain.repository

import com.example.zehnchin.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setMusicEnabled(enabled: Boolean)
    suspend fun setHapticEnabled(enabled: Boolean)
    suspend fun setDailyReminderEnabled(enabled: Boolean)
}
