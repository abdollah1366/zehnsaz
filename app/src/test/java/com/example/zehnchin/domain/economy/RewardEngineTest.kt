package com.example.zehnchin.domain.economy

import com.example.zehnchin.data.local.dao.PlayerDao
import com.example.zehnchin.data.local.entities.*
import com.example.zehnchin.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RewardEngineTest {

    private lateinit var fakePlayerDao: FakePlayerDao
    private lateinit var rewardEngine: RewardEngine

    @Before
    fun setup() {
        fakePlayerDao = FakePlayerDao()
        rewardEngine = RewardEngine(fakePlayerDao)
    }

    @Test
    fun testRewardAccumulationAndLevelUp() = runBlocking {
        val initialProfile = PlayerProfileEntity("default_player", level = 1, xp = 90)
        fakePlayerDao.savePlayerProfile(initialProfile)
        
        // Reward that triggers level up
        val reward = Reward(xp = 20, coins = 50)
        // Level 1 requires 100 XP (BalanceConfig.calculateXPForLevel(1))
        
        rewardEngine.applyReward(reward, RewardSource.LEVEL_REWARD)
        
        val updatedProfile = fakePlayerDao.profiles["default_player"]
        assertEquals(2, updatedProfile?.level)
        assertEquals(10, updatedProfile?.xp)
        assertEquals(50, updatedProfile?.totalCoins)
    }

    private class FakePlayerDao : PlayerDao {
        val profiles = mutableMapOf<String, PlayerProfileEntity>()
        
        override fun getPlayerProfile(playerId: String): Flow<PlayerProfileEntity?> = flowOf(profiles[playerId])
        
        override suspend fun savePlayerProfile(profile: PlayerProfileEntity) {
            profiles[profile.playerId] = profile
        }

        override fun getInventory(playerId: String): Flow<List<InventoryItemEntity>> = flowOf(emptyList())
        override suspend fun saveInventoryItem(item: InventoryItemEntity) {}
        override fun getAllAchievements(): Flow<List<AchievementEntity>> = flowOf(emptyList())
        override suspend fun updateAchievement(achievement: AchievementEntity) {}
        override suspend fun insertAchievements(achievements: List<AchievementEntity>) {}
        override suspend fun addTransaction(transaction: CurrencyTransactionEntity) {}
        override fun getRecentTransactions(): Flow<List<CurrencyTransactionEntity>> = flowOf(emptyList())
        override fun getAllEntitlements(): Flow<List<EntitlementEntity>> = flowOf(emptyList())
        override suspend fun saveEntitlement(entitlement: EntitlementEntity) {}
    }
}
