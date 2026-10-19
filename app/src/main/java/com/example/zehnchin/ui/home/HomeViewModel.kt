package com.example.zehnchin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.data.local.dao.PlayerDao
import com.example.zehnchin.data.local.entities.PlayerProfileEntity
import com.example.zehnchin.domain.progression.AchievementManager
import com.example.zehnchin.domain.progression.DailyRewardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playerDao: PlayerDao,
    private val dailyRewardManager: DailyRewardManager,
    private val achievementManager: AchievementManager
) : ViewModel() {

    val playerProfile: StateFlow<PlayerProfileEntity?> = playerDao.getPlayerProfile("default_player")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isDailyRewardClaimed = MutableStateFlow(false)
    val isDailyRewardClaimed = _isDailyRewardClaimed.asStateFlow()

    init {
        viewModelScope.launch {
            achievementManager.initializeAchievements()
            updateDailyRewardStatus()
        }
    }

    private suspend fun updateDailyRewardStatus() {
        _isDailyRewardClaimed.value = dailyRewardManager.getDailyRewardStatus() == DailyRewardManager.RewardStatus.ALREADY_CLAIMED
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            if (dailyRewardManager.claimDailyReward()) {
                updateDailyRewardStatus()
            }
        }
    }
}