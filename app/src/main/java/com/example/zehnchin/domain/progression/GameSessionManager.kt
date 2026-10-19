package com.example.zehnchin.domain.progression

import com.example.zehnchin.data.local.dao.LevelDao
import com.example.zehnchin.data.local.entities.LevelProgressEntity
import com.example.zehnchin.domain.economy.EconomyConfig
import com.example.zehnchin.domain.economy.RewardEngine
import com.example.zehnchin.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.first

@Singleton
class GameSessionManager @Inject constructor(
    private val levelDao: LevelDao,
    private val rewardEngine: RewardEngine,
    private val achievementManager: AchievementManager
) {
    suspend fun completeLevel(level: Level, stars: Int, score: Int) {
        val previousProgress = levelDao.getLevelProgress(level.id).first()
        val isFirstCompletion = previousProgress == null || !previousProgress.isCompleted
        val isNewStarRecord = previousProgress == null || stars > previousProgress.stars

        // Save Progress
        val progress = LevelProgressEntity(
            levelId = level.id,
            isCompleted = true,
            stars = maxOf(stars, previousProgress?.stars ?: 0),
            bestScore = maxOf(score, previousProgress?.bestScore ?: 0)
        )
        levelDao.saveProgress(progress)

        // Apply Rewards only on first completion
        if (isFirstCompletion) {
            val baseReward = EconomyConfig.getLevelReward(level.chapterId, level.difficulty, stars)
            rewardEngine.applyReward(baseReward, RewardSource.LEVEL_REWARD)
            
            // Notify Achievements
            achievementManager.onEvent(GameEvent.LEVEL_COMPLETED, 1)
        }

        // Apply Star Bonus only when reaching 3 stars for the first time
        // Note: baseReward already includes stars, but we can add an extra milestone reward here if needed.
        // For now, let's keep it simple. If we want a separate bonus:
        if (stars >= 3 && isNewStarRecord) {
            // achievementManager already handles STAR_EARNED
            achievementManager.onEvent(GameEvent.STAR_EARNED, 1)
        }
    }
}
