package com.barati.zehnsaz.domain.monetization.subscription

import com.barati.zehnsaz.data.local.dao.PlayerDao
import com.barati.zehnsaz.data.local.entities.*
import com.barati.zehnsaz.domain.economy.RewardEngine
import com.barati.zehnsaz.domain.model.*
import com.barati.zehnsaz.domain.monetization.AdManager
import com.barati.zehnsaz.domain.monetization.AnalyticsProvider
import com.barati.zehnsaz.domain.monetization.EntitlementManager
import com.barati.zehnsaz.domain.monetization.MonetizationConfig
import com.barati.zehnsaz.domain.monetization.RewardedAdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SubscriptionManagerTest {

    private lateinit var dao: InMemoryPlayerDao
    private lateinit var store: ScriptedStore
    private lateinit var state: SubscriptionState
    private lateinit var manager: SubscriptionManager

    @Before
    fun setup() {
        dao = InMemoryPlayerDao()
        store = ScriptedStore()
        state = SubscriptionState()
        manager = SubscriptionManager(store, EntitlementManager(dao), state, NoAnalytics)
    }

    @Test
    fun successfulPurchaseActivatesAndIsCached() = runBlocking {
        val outcome = manager.subscribe("monthly")

        assertEquals(PurchaseResult.SUCCESS, outcome.result)
        assertTrue(state.isSubscribed)
        assertEquals("monthly", state.status.value.planId)
        assertTrue(dao.isActive(EntitlementType.PREMIUM_SUBSCRIPTION))
    }

    @Test
    fun cancelledPurchaseLeavesUserUnsubscribed() = runBlocking {
        store.nextResult = PurchaseResult.CANCELLED

        manager.subscribe("monthly")

        assertFalse(state.isSubscribed)
        assertFalse(dao.isActive(EntitlementType.PREMIUM_SUBSCRIPTION))
    }

    @Test
    fun offlineStartUsesCacheAndFailedRefreshKeepsIt() = runBlocking {
        dao.saveEntitlement(EntitlementEntity(EntitlementType.PREMIUM_SUBSCRIPTION, isActive = true))
        store.queryFails = true

        manager.loadCachedStatus()
        manager.refresh()

        assertTrue(state.isSubscribed)
        assertFalse(state.status.value.isVerifiedNow)
    }

    @Test
    fun expiredSubscriptionIsRevokedOnRefresh() = runBlocking {
        manager.subscribe("monthly")
        store.active = emptyList() // Bazaar no longer lists it (expired or refunded)

        manager.refresh()

        assertFalse(state.isSubscribed)
        assertTrue(state.status.value.isVerifiedNow)
        assertFalse(dao.isActive(EntitlementType.PREMIUM_SUBSCRIPTION))
    }

    @Test
    fun secondPurchaseIsRefusedWhileActive() = runBlocking {
        manager.subscribe("monthly")
        assertEquals(PurchaseResult.ALREADY_OWNED, manager.subscribe("yearly").result)
    }

    @Test
    fun subscribersAreNotShownAds() = runBlocking {
        val adManager = AdManager(AlwaysReadyAd, NoRewards, dao, NoAnalytics, Dispatchers.Unconfined, state)
        assertTrue(adManager.canShowAd())

        manager.subscribe("monthly")

        assertFalse(adManager.canShowAd())
    }

    @Test
    fun levelSkipAdIsOfferedOnlyAfterRepeatedFailures() {
        val adManager = AdManager(AlwaysReadyAd, NoRewards, dao, NoAnalytics, Dispatchers.Unconfined, state)
        val level = "2:5"

        repeat(MonetizationConfig.FAILURES_BEFORE_AD_SKIP - 1) { adManager.onLevelFailed(level) }
        assertFalse(adManager.canOfferLevelSkip(level))

        adManager.onLevelFailed(level)
        assertTrue(adManager.canOfferLevelSkip(level))

        adManager.onLevelPassed(level)
        assertFalse(adManager.canOfferLevelSkip(level))
    }

    private class ScriptedStore : SubscriptionProvider {
        override val providerName = "test"
        var nextResult = PurchaseResult.SUCCESS
        var queryFails = false
        var active: List<ActiveSubscription> = emptyList()

        override fun isAvailable() = true
        override suspend fun getPlans(planIds: List<String>) = emptyList<SubscriptionPlan>()
        override suspend fun subscribe(planId: String): SubscriptionPurchaseOutcome {
            if (nextResult != PurchaseResult.SUCCESS) return SubscriptionPurchaseOutcome(nextResult)
            val sub = ActiveSubscription(planId, "token", "order", 0L, providerName)
            active = listOf(sub)
            return SubscriptionPurchaseOutcome(PurchaseResult.SUCCESS, sub)
        }
        override suspend fun queryActiveSubscriptions(): Result<List<ActiveSubscription>> =
            if (queryFails) Result.failure(IllegalStateException("offline")) else Result.success(active)
    }

    private object AlwaysReadyAd : RewardedAdProvider {
        override fun isAvailable() = true
        override fun load() {}
        override fun show(placement: AdPlacement, onResult: (AdResult) -> Unit) = onResult(AdResult.COMPLETED)
        override fun getStatus() = AdStatus.READY
    }

    private object NoRewards : RewardEngine(InMemoryPlayerDao()) {
        override suspend fun applyReward(reward: Reward, source: RewardSource, playerId: String) {}
    }

    private object NoAnalytics : AnalyticsProvider {
        override fun trackEvent(eventName: String, params: Map<String, Any>) {}
        override fun trackPurchase(productId: String, amount: Double, currency: String) {}
        override fun trackAdEvent(placement: AdPlacement, result: AdResult) {}
    }

    private class InMemoryPlayerDao : PlayerDao {
        private val entitlements = MutableStateFlow<Map<EntitlementType, EntitlementEntity>>(emptyMap())
        fun isActive(type: EntitlementType) = entitlements.value[type]?.isActive == true

        override fun getAllEntitlements(): Flow<List<EntitlementEntity>> = entitlements.map { it.values.toList() }
        override suspend fun saveEntitlement(entitlement: EntitlementEntity) {
            entitlements.value = entitlements.value + (entitlement.type to entitlement)
        }
        override fun getPlayerProfile(playerId: String): Flow<PlayerProfileEntity?> = flowOf(null)
        override suspend fun savePlayerProfile(profile: PlayerProfileEntity) {}
        override fun getInventory(playerId: String): Flow<List<InventoryItemEntity>> = flowOf(emptyList())
        override suspend fun saveInventoryItem(item: InventoryItemEntity) {}
        override fun getRecentTransactions(): Flow<List<CurrencyTransactionEntity>> = flowOf(emptyList())
        override suspend fun addTransaction(transaction: CurrencyTransactionEntity) {}
        override fun getAllAchievements(): Flow<List<AchievementEntity>> = flowOf(emptyList())
        override suspend fun updateAchievement(achievement: AchievementEntity) {}
        override suspend fun insertAchievements(achievements: List<AchievementEntity>) {}
    }
}
