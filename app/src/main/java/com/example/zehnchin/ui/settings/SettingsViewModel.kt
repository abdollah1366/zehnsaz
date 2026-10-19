package com.example.zehnchin.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.domain.repository.SettingsRepository
import com.example.zehnchin.reminder.DailyReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val settings = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.example.zehnchin.domain.model.AppSettings()
        )

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticEnabled(enabled)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDailyReminderEnabled(enabled)
        }
        if (enabled) DailyReminder.schedule(appContext) else DailyReminder.cancel(appContext)
    }
}
