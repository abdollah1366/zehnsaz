package com.example.zehnchin.domain.economy

import com.example.zehnchin.domain.model.ItemType
import com.example.zehnchin.domain.model.Reward

object EconomyConfig {
    const val XP_BASE = BalanceConfig.XP_BASE
    const val XP_MULTIPLIER = BalanceConfig.XP_MULTIPLIER

    fun calculateXPForLevel(level: Int): Int = BalanceConfig.calculateXPForLevel(level)

    val LEVEL_COMPLETION_REWARD = Reward(xp = 50, coins = 20) // Default for backward compatibility if used directly
    val THREE_STAR_BONUS = Reward(xp = 25, coins = 10, gems = 1)

    val DAILY_REWARDS = BalanceConfig.DAILY_REWARDS
    val BOOSTER_COSTS = BalanceConfig.BOOSTER_COSTS
    
    fun getLevelReward(chapterId: Int, difficulty: Int, stars: Int): Reward = 
        BalanceConfig.getLevelReward(chapterId, difficulty, stars)
}
