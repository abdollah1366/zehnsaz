package com.barati.zehnsaz.domain.economy

import org.junit.Test
import java.util.*

class EconomySimulatorTest {

    @Test
    fun `simulate free player 30 day progression`() {
        println("=== 30-Day Free Player Simulation ===")
        
        var currentLevel = 1
        var currentXP = 0
        var totalCoins = 0
        var totalGems = 0
        var totalCompletedLevels = 0
        
        val days = 30
        val levelsPerDay = 5
        val avgStars = 2
        
        println("| Day | Level | XP | Coins | Gems | Total Levels |")
        println("|---|---|---|---|---|---|")
        
        for (day in 1..days) {
            // Daily Reward
            val dailyReward = BalanceConfig.DAILY_REWARDS[(day - 1) % 7]
            totalCoins += dailyReward.coins
            totalGems += dailyReward.gems
            
            // Gameplay
            for (l in 1..levelsPerDay) {
                totalCompletedLevels++
                val chapterId = (totalCompletedLevels / 10 + 1).coerceAtMost(6)
                val difficulty = (totalCompletedLevels % 10 + 1).coerceAtMost(10)
                
                val reward = BalanceConfig.getLevelReward(chapterId, difficulty, avgStars)
                currentXP += reward.xp
                totalCoins += reward.coins
                totalGems += reward.gems
                
                // Level Up
                while (currentXP >= BalanceConfig.calculateXPForLevel(currentLevel)) {
                    currentXP -= BalanceConfig.calculateXPForLevel(currentLevel)
                    currentLevel++
                }
            }
            
            if (day == 7 || day == 14 || day == 30) {
                println("| $day | $currentLevel | $currentXP | $totalCoins | $totalGems | $totalCompletedLevels |")
            }
        }
        
        println("\nSimulation Result:")
        println("Final Level: $currentLevel")
        println("Total Coins: $totalCoins")
        println("Total Gems: $totalGems")
        println("Total Levels Completed: $totalCompletedLevels")
        
        // Assertions for starvation/inflation
        assert(currentLevel >= 10) { "XP progression too slow" }
        assert(totalCoins > 5000) { "Coin starvation" }
        assert(totalCoins < 100000) { "Coin inflation" }
    }

    @Test
    fun `print XP progression table`() {
        println("\n=== XP Progression Table ===")
        println("| Level | XP Required | Cumulative XP |")
        println("|---|---|---|")
        var cumulativeXP = 0
        for (level in 1..20) {
            val req = BalanceConfig.calculateXPForLevel(level)
            cumulativeXP += req
            println("| $level | $req | $cumulativeXP |")
        }
    }
}
