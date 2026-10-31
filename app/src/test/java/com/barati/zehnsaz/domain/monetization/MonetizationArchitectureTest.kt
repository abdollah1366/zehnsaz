package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.domain.economy.RewardEngine
import com.barati.zehnsaz.domain.model.*
import com.barati.zehnsaz.data.local.dao.PlayerDao
import com.barati.zehnsaz.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MonetizationArchitectureTest {

    private lateinit var adProvider: FakeRewardedAdProvider
    private lateinit var rewardEngine: FakeRewardEngine
    private lateinit var playerDao: FakePlayerDao
    private lateinit var analyticsProvider: FakeAnalyticsProvider
    private lateinit var purchaseProvider: FakePurchaseProvider
    private lateinit var entitlementManager: FakeEntitlementManager

    private lateinit var adManager: AdManager
    private lateinit var shopManager: ShopManager

    @Before
    fun setup() {
        adProvider = FakeRewardedAdProvider()
        rewardEngine = FakeRewardEngine()
        playerDao = FakePlayerDao()
        analyticsProvider = FakeAnalyticsProvider()
        purchaseProvider = FakePurchaseProvider()
        entitlementManager = FakeEntitlementManager()

        val testDispatcher = Dispatchers.Unconfined

        adManager = AdManager(adProvider, rewardEngine, playerDao, analyticsProvider, testDispatcher)
        shopManager = ShopManager(purchaseProvider, rewardEngine, entitlementManager, analyticsProvider, testDispatcher)
    }

    @Test
    fun testAdDailyLimit() = runBlocking {
        adProvider.nextResult = AdResult.COMPLETED

        repeat(MonetizationConfig.REWARDED_AD_DAILY_LIMIT) {
            adManager.showRewardedAd(AdPlacement.SHOP) { }
        }

        assertFalse(adManager.canShowAd())
    }

    @Test
    fun testAdCooldown() = runBlocking {
        adProvider.nextResult = AdResult.COMPLETED
        
        adManager.showRewardedAd(AdPlacement.SHOP) { }
        
        // Immediate next ad should be blocked by cooldown
        assertFalse(adManager.canShowAd())
    }

    @Test
    fun testDuplicateAdRewardProtection() = runBlocking {
        var rewardsCount = 0
        val customRewardEngine = object : FakeRewardEngine() {
            override suspend fun applyReward(reward: Reward, source: RewardSource, playerId: String) {
                rewardsCount++
            }
        }
        val customAdManager = AdManager(adProvider, customRewardEngine, playerDao, analyticsProvider, Dispatchers.Unconfined)
        
        // Mock provider that calls callback twice
        val buggyProvider = object : FakeRewardedAdProvider() {
            override fun show(placement: AdPlacement, onResult: (AdResult) -> Unit) {
                onResult(AdResult.COMPLETED)
                onResult(AdResult.COMPLETED)
            }
        }
        val customAdManager2 = AdManager(buggyProvider, customRewardEngine, playerDao, analyticsProvider, Dispatchers.Unconfined)

        customAdManager2.showRewardedAd(AdPlacement.SHOP) { }
        
        assertEquals("Reward should be granted only once", 1, rewardsCount)
    }

    @Test
    fun testAdFailureDoesNotGrantReward() = runBlocking {
        var rewardGranted = false
        val customRewardEngine = object : FakeRewardEngine() {
            override suspend fun applyReward(reward: Reward, source: RewardSource, playerId: String) {
                rewardGranted = true
            }
        }
        val customAdManager = AdManager(adProvider, customRewardEngine, playerDao, analyticsProvider, Dispatchers.Unconfined)
        
        adProvider.nextResult = AdResult.FAILED
        customAdManager.showRewardedAd(AdPlacement.SHOP) { }
        
        assertFalse(rewardGranted)
    }

    @Test
    fun testPurchaseConsumableGrantsReward() = runBlocking {
        purchaseProvider.nextResult = PurchaseResult.SUCCESS
        
        shopManager.purchaseProduct("coins_100") { }

        assertEquals(100, rewardEngine.lastReward?.coins)
    }

    @Test
    fun testPurchaseNonConsumableGrantsEntitlement() = runBlocking {
        purchaseProvider.nextResult = PurchaseResult.SUCCESS
        
        shopManager.purchaseProduct("remove_ads") { }

        assertTrue(entitlementManager.activeEntitlements.contains(EntitlementType.REMOVE_ADS))
    }

    // --- Manual Fakes ---

    private open class FakeRewardedAdProvider : RewardedAdProvider {
        var nextResult: AdResult = AdResult.COMPLETED
        override fun isAvailable(): Boolean = true
        override fun load() {}
        override fun show(placement: AdPlacement, onResult: (AdResult) -> Unit) {
            onResult(nextResult)
        }
        override fun getStatus(): AdStatus = AdStatus.IDLE
    }

    private open class FakeRewardEngine : RewardEngine(mockPlayerDao()) {
        var lastReward: Reward? = null
        override suspend fun applyReward(reward: Reward, source: RewardSource, playerId: String) {
            lastReward = reward
        }
    }

    private class FakePurchaseProvider : PurchaseProvider {
        var nextResult: PurchaseResult = PurchaseResult.SUCCESS
        override suspend fun getProducts(productIds: List<String>): List<Product> = emptyList()
        override fun purchase(productId: String, onResult: (PurchaseResult) -> Unit) {
            onResult(nextResult)
        }
        override fun restorePurchases(onResult: (List<String>) -> Unit) {
            onResult(emptyList())
        }
        override fun isAvailable(): Boolean = true
    }

    private open class FakeEntitlementManager : EntitlementManager(mockPlayerDao()) {
        val activeEntitlements = mutableSetOf<EntitlementType>()
        override suspend fun grantEntitlement(type: EntitlementType) {
            activeEntitlements.add(type)
        }
    }

    private class FakePlayerDao : PlayerDao {
        override fun getPlayerProfile(playerId: String): Flow<PlayerProfileEntity?> = flowOf(null)
        override suspend fun savePlayerProfile(profile: PlayerProfileEntity) {}
        override fun getInventory(playerId: String): Flow<List<InventoryItemEntity>> = flowOf(emptyList())
        override suspend fun saveInventoryItem(item: InventoryItemEntity) {}
        override fun getRecentTransactions(): Flow<List<CurrencyTransactionEntity>> = flowOf(emptyList())
        override suspend fun addTransaction(transaction: CurrencyTransactionEntity) {}
        override fun getAllAchievements(): Flow<List<AchievementEntity>> = flowOf(emptyList())
        override suspend fun updateAchievement(achievement: AchievementEntity) {}
        override suspend fun insertAchievements(achievements: List<AchievementEntity>) {}
        override fun getAllEntitlements(): Flow<List<EntitlementEntity>> = flowOf(emptyList())
        override suspend fun saveEntitlement(entitlement: EntitlementEntity) {}
    }

    private class FakeAnalyticsProvider : AnalyticsProvider {
        override fun trackEvent(eventName: String, params: Map<String, Any>) {}
        override fun trackPurchase(productId: String, amount: Double, currency: String) {}
        override fun trackAdEvent(placement: AdPlacement, result: AdResult) {}
    }

    companion object {
        fun mockPlayerDao(): PlayerDao = object : PlayerDao {
            override fun getPlayerProfile(playerId: String) = flowOf(null)
            override suspend fun savePlayerProfile(profile: PlayerProfileEntity) {}
            override fun getInventory(playerId: String) = flowOf(emptyList<InventoryItemEntity>())
            override suspend fun saveInventoryItem(item: InventoryItemEntity) {}
            override fun getRecentTransactions() = flowOf(emptyList<CurrencyTransactionEntity>())
            override suspend fun addTransaction(transaction: CurrencyTransactionEntity) {}
            override fun getAllAchievements() = flowOf(emptyList<AchievementEntity>())
            override suspend fun updateAchievement(achievement: AchievementEntity) {}
            override suspend fun insertAchievements(achievements: List<AchievementEntity>) {}
            override fun getAllEntitlements() = flowOf(emptyList<EntitlementEntity>())
            override suspend fun saveEntitlement(entitlement: EntitlementEntity) {}
        }
    }
}
