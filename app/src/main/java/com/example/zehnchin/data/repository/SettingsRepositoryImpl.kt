package com.example.zehnchin.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.zehnchin.domain.model.AppSettings
import com.example.zehnchin.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
    }

    override fun getSettings(): Flow<AppSettings> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                AppSettings(
                    soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                    musicEnabled = preferences[PreferencesKeys.MUSIC_ENABLED] ?: true,
                    hapticEnabled = preferences[PreferencesKeys.HAPTIC_ENABLED] ?: true,
                    dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: false
                )
            }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_ENABLED] = enabled
        }
    }

    override suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_ENABLED] = enabled
        }
    }

    override suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
        }
    }
}
