package com.example.zehnchin.domain.economy

import com.example.zehnchin.domain.model.ItemType
import com.example.zehnchin.domain.model.Reward

object BalanceConfig {
    // XP Progression
    const val XP_BASE = 100
    const val XP_MULTIPLIER = 1.25 // Reduced from 1.5 to prevent too steep curve
    
    fun calculateXPForLevel(level: Int): Int {
        // formula: base * multiplier^(level-1), capped at reasonable max
        return (XP_BASE * Math.pow(XP_MULTIPLIER, (level - 1).toDouble())).toInt().coerceAtMost(10000)
    }

    // Level Rewards (Chapter 1 base)
    const val LEVEL_XP_BASE = 50
    const val LEVEL_COINS_BASE = 20 // Increased from 10
    
    fun getLevelReward(chapterId: Int, difficulty: Int, stars: Int): Reward {
        // Scaled rewards based on chapter and difficulty
        val scale = 1.0 + (chapterId - 1) * 0.2 + (difficulty - 1) * 0.1
        
        val xp = (LEVEL_XP_BASE * scale).toInt()
        val coins = (LEVEL_COINS_BASE * scale).toInt()
        
        // Star bonuses
        val starXP = (stars * 10 * scale).toInt()
        val starCoins = (stars * 5 * scale).toInt()
        val starGems = if (stars >= 3) 1 else 0
        
        return Reward(
            xp = xp + starXP,
            coins = coins + starCoins,
            gems = starGems
        )
    }

    // Booster Costs
    val BOOSTER_COSTS = mapOf(
        ItemType.HINT to 150,
        ItemType.UNDO to 200,
        ItemType.SHUFFLE to 250,
        ItemType.TIME_BOOST to 400
    )

    // Daily Rewards
    val DAILY_REWARDS = listOf(
        Reward(coins = 100),
        Reward(coins = 150),
        Reward(items = mapOf(ItemType.HINT to 1)),
        Reward(coins = 200),
        Reward(gems = 5),
        Reward(items = mapOf(ItemType.UNDO to 1)),
        Reward(coins = 500, gems = 10, items = mapOf(ItemType.LIFE to 1))
    )

    // Achievement Rewards
    fun getAchievementReward(difficulty: Int): Reward {
        return when (difficulty) {
            1 -> Reward(coins = 100, xp = 200)
            2 -> Reward(coins = 250, xp = 500, gems = 2)
            3 -> Reward(coins = 1000, xp = 2000, gems = 10)
            else -> Reward(coins = 100, xp = 200)
        }
    }

    // Rewarded Ad Reward
    val REWARDED_AD_COINS = 50
    val REWARDED_AD_GEMS = 1

    fun getAdReward(placement: com.example.zehnchin.domain.model.AdPlacement): Reward {
        return when (placement) {
            com.example.zehnchin.domain.model.AdPlacement.LEVEL_COMPLETED -> Reward(coins = 30) // Bonus
            com.example.zehnchin.domain.model.AdPlacement.LEVEL_FAILED -> Reward(items = mapOf(ItemType.UNDO to 1))
            com.example.zehnchin.domain.model.AdPlacement.SHOP -> Reward(coins = 50)
            com.example.zehnchin.domain.model.AdPlacement.DAILY_REWARD -> Reward(coins = 20) // Extra
            com.example.zehnchin.domain.model.AdPlacement.EXTRA_HINT -> Reward(items = mapOf(ItemType.HINT to 1))
        }
    }
    
    // Chapter Unlock Requirements
    fun getChapterUnlockRequirement(chapterId: Int): Int {
        return when (chapterId) {
            2 -> 5  // 5 levels from Ch 1
            3 -> 10 // 10 levels total
            4 -> 20 // 20 levels total
            5 -> 30 // 30 levels total
            6 -> 50 // 50 levels total
            else -> 0
        }
    }
}
