package com.example.zehnchin.domain.progression

import com.example.zehnchin.data.local.dao.PlayerDao
import com.example.zehnchin.data.local.entities.PlayerProfileEntity
import com.example.zehnchin.domain.economy.EconomyConfig
import com.example.zehnchin.domain.economy.RewardEngine
import com.example.zehnchin.domain.model.RewardSource
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyRewardManager @Inject constructor(
    private val playerDao: PlayerDao,
    private val rewardEngine: RewardEngine
) {
    enum class RewardStatus {
        AVAILABLE,
        ALREADY_CLAIMED,
        LOCKED
    }

    suspend fun getDailyRewardStatus(playerId: String = "default_player"): RewardStatus {
        val profile = playerDao.getPlayerProfile(playerId).first() ?: return RewardStatus.AVAILABLE
        
        val lastClaim = Calendar.getInstance().apply { timeInMillis = profile.lastLoginDate }
        val today = Calendar.getInstance()
        
        return if (isSameDay(lastClaim, today)) {
            RewardStatus.ALREADY_CLAIMED
        } else {
            RewardStatus.AVAILABLE
        }
    }

    suspend fun claimDailyReward(playerId: String = "default_player"): Boolean {
        if (getDailyRewardStatus(playerId) != RewardStatus.AVAILABLE) return false
        
        val profile = playerDao.getPlayerProfile(playerId).first() ?: PlayerProfileEntity(playerId)
        val today = Calendar.getInstance()
        
        // Calculate Streak
        val lastClaim = Calendar.getInstance().apply { timeInMillis = profile.lastLoginDate }
        val isConsecutive = isNextDay(lastClaim, today)
        
        val newStreak = if (isConsecutive) profile.currentStreak + 1 else 1
        val dayIndex = (newStreak - 1) % 7
        val reward = EconomyConfig.DAILY_REWARDS[dayIndex]
        
        val updatedProfile = profile.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(profile.longestStreak, newStreak),
            lastLoginDate = today.timeInMillis
        )
        playerDao.savePlayerProfile(updatedProfile)
        
        rewardEngine.applyReward(reward, RewardSource.DAILY_REWARD, playerId)
        return true
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isNextDay(c1: Calendar, c2: Calendar): Boolean {
        val yesterday = c2.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(c1, yesterday)
    }
}