package com.barati.zehnsaz.domain.progression

import com.barati.zehnsaz.data.local.dao.PlayerDao
import com.barati.zehnsaz.data.local.entities.AchievementEntity
import com.barati.zehnsaz.domain.economy.BalanceConfig
import com.barati.zehnsaz.domain.economy.RewardEngine
import com.barati.zehnsaz.domain.model.GameEvent
import com.barati.zehnsaz.domain.model.Reward
import com.barati.zehnsaz.domain.model.RewardSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementManager @Inject constructor(
    private val playerDao: PlayerDao,
    private val rewardEngine: RewardEngine
) {
    private val mutex = Mutex()

    suspend fun onEvent(event: GameEvent, progress: Int = 1) {
        mutex.withLock {
            val achievements = playerDao.getAllAchievements().first()
            
            achievements.forEach { achievement ->
                if (!achievement.isUnlocked) {
                    val isRelevant = when (achievement.id) {
                        "COMPLETE_10_LEVELS" -> event == GameEvent.LEVEL_COMPLETED
                        "THREE_STAR_LEVEL" -> event == GameEvent.STAR_EARNED
                        else -> false
                    }
                    
                    if (isRelevant) {
                        val newProgress = achievement.currentProgress + progress
                        if (newProgress >= achievement.targetProgress) {
                            unlockAchievement(achievement)
                        } else {
                            playerDao.updateAchievement(achievement.copy(currentProgress = newProgress))
                        }
                    }
                }
            }
        }
    }

    private suspend fun unlockAchievement(achievement: AchievementEntity) {
        val updated = achievement.copy(
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis(),
            currentProgress = achievement.targetProgress
        )
        playerDao.updateAchievement(updated)
        
        // Give Reward based on achievement ID or tier
        val difficulty = when (achievement.id) {
            "COMPLETE_10_LEVELS" -> 2
            "THREE_STAR_LEVEL" -> 2
            else -> 1
        }
        val reward = BalanceConfig.getAchievementReward(difficulty)
        rewardEngine.applyReward(reward, RewardSource.ACHIEVEMENT)
    }

    suspend fun initializeAchievements() {
        mutex.withLock {
            val existing = playerDao.getAllAchievements().first()
            if (existing.isEmpty()) {
                val initial = listOf(
                    AchievementEntity("FIRST_STEP", "اولین قدم", "اولین مرحله را با موفقیت تمام کن", targetProgress = 1),
                    AchievementEntity("COMPLETE_10_LEVELS", "ده‌تایی", "۱۰ مرحله را تمام کن", targetProgress = 10),
                    AchievementEntity("THREE_STAR_LEVEL", "درخشان", "یک مرحله را با ۳ ستاره تمام کن", targetProgress = 1)
                )
                playerDao.insertAchievements(initial)
            }
        }
    }
}
