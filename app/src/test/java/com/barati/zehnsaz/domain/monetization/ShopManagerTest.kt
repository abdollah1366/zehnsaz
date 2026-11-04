package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.data.local.dao.PlayerDao
import com.barati.zehnsaz.data.local.dao.PurchaseDao
import com.barati.zehnsaz.data.local.entities.*
import com.barati.zehnsaz.domain.economy.RewardEngine
import com.barati.zehnsaz.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShopManagerTest {

    private lateinit var store: ScriptedStore
    private lateinit var ledger: InMemoryPurchaseDao
    private lateinit var playerDao: InMemoryPlayerDao
    private lateinit var rewards: CountingRewardEngine
    private lateinit var verifier: ScriptedVerifier
    private lateinit var shop: ShopManager

    @Before
    fun setup() {
        store = ScriptedStore()
        ledger = InMemoryPurchaseDao()
        playerDao = InMemoryPlayerDao()
        rewards = CountingRewardEngine()
        verifier = ScriptedVerifier()
        shop = newShop()
    }

    /** A fresh ShopManager over the same store and database, like the app starting again. */
    private fun newShop() = ShopManager(store, ledger, verifier, rewards, EntitlementManager(playerDao), NoAnalytics)

    @Test
    fun consumableIsGrantedOnceAndConsumed() = runBlocking {
        assertEquals(PurchaseResult.SUCCESS, shop.purchase("coins_100"))

        assertEquals(listOf(100), rewards.coins)
        assertTrue(store.owned.isEmpty())
        assertTrue(ledger.records.values.single().consumed)

        // A later restore must not pay out again.
        assertEquals(0, shop.restorePurchases().restoredCount)
        assertEquals(listOf(100), rewards.coins)
    }

    @Test
    fun consumableCanBeBoughtAgain() = runBlocking {
        shop.purchase("coins_100")
        shop.purchase("coins_100")

        assertEquals(listOf(100, 100), rewards.coins)
    }

    @Test
    fun failedConsumeIsFinishedByRestoreWithoutDoubleReward() = runBlocking {
        store.consumeWorks = false
        assertEquals(PurchaseResult.SUCCESS, shop.purchase("gems_10"))
        assertEquals(1, store.owned.size) // still owned in Bazaar

        store.consumeWorks = true
        shop.restorePurchases()

        assertEquals(1, rewards.calls)
        assertTrue(store.owned.isEmpty())
        assertTrue(ledger.records.values.single().consumed)
    }

    @Test
    fun purchasePaidButNotDeliveredIsDeliveredOnNextStart() = runBlocking {
        // The app died after Bazaar took the payment and before anything was recorded.
        store.owned += OwnedPurchase("coins_500", "tok_crash", "order", 0L, "test")

        val restore = newShop().restorePurchases()

        assertEquals(1, restore.restoredCount)
        assertEquals(listOf(500), rewards.coins)
        assertTrue(store.owned.isEmpty())
    }

    @Test
    fun removeAdsIsKeptAndRestoredAfterReinstall() = runBlocking {
        assertEquals(PurchaseResult.SUCCESS, shop.purchase("remove_ads"))
        assertTrue(playerDao.isActive(EntitlementType.REMOVE_ADS))
        assertEquals(1, store.owned.size) // one-time products are never consumed
        assertEquals(PurchaseResult.ALREADY_OWNED, shop.purchase("remove_ads"))

        // Reinstall: local data is gone, Bazaar still knows the purchase.
        ledger.records.clear()
        playerDao.clear()
        val restore = newShop().restorePurchases()

        assertEquals(1, restore.restoredCount)
        assertTrue(playerDao.isActive(EntitlementType.REMOVE_ADS))
    }

    @Test
    fun rejectedPurchaseIsNotDeliveredOrConsumed() = runBlocking {
        verifier.result = VerificationResult.REJECTED

        assertEquals(PurchaseResult.VERIFICATION_FAILED, shop.purchase("coins_100"))

        assertEquals(0, rewards.calls)
        assertTrue(ledger.records.isEmpty())
        assertEquals(1, store.owned.size)
    }

    @Test
    fun unreachableVerifierStillDelivers() = runBlocking {
        verifier.result = VerificationResult.UNAVAILABLE

        assertEquals(PurchaseResult.SUCCESS, shop.purchase("coins_100"))
        assertEquals(1, rewards.calls)
    }

    @Test
    fun cancelledPurchaseGivesNothing() = runBlocking {
        store.nextResult = PurchaseResult.CANCELLED

        assertEquals(PurchaseResult.CANCELLED, shop.purchase("coins_100"))
        assertEquals(0, rewards.calls)
    }

    @Test
    fun failedPurchaseFinishesAnEarlierStuckOne() = runBlocking {
        store.owned += OwnedPurchase("coins_100", "tok_stuck", "order", 0L, "test")
        store.nextResult = PurchaseResult.FAILED // Bazaar: "already owned"

        assertEquals(PurchaseResult.FAILED, shop.purchase("coins_100"))

        assertEquals(listOf(100), rewards.coins) // the stuck one was delivered
        assertTrue(store.owned.isEmpty())
    }

    @Test
    fun storeUnavailable() = runBlocking {
        store.available = false

        assertEquals(PurchaseResult.NOT_AVAILABLE, shop.purchase("coins_100"))
        assertFalse(shop.restorePurchases().reachedStore)
    }

    @Test
    fun offlineRestoreChangesNothing() = runBlocking {
        store.queryFails = true

        val restore = shop.restorePurchases()

        assertFalse(restore.reachedStore)
        assertEquals(0, rewards.calls)
    }

    @Test
    fun unknownProductIsRefused() = runBlocking {
        assertEquals(PurchaseResult.FAILED, shop.purchase("not_in_catalog"))
        assertEquals(0, store.purchaseCalls)
    }

    @Test
    fun productsKeepCatalogOrder() = runBlocking {
        shop.refreshProducts()

        assertEquals(ProductCatalog.productIds, shop.products.value.map { it.productId })
    }

    // --- Fakes ---

    private class ScriptedStore : PurchaseProvider {
        override val providerName = "test"
        var available = true
        var nextResult = PurchaseResult.SUCCESS
        var consumeWorks = true
        var queryFails = false
        var purchaseCalls = 0
        val owned = mutableListOf<OwnedPurchase>()
        private var counter = 0

        override fun isAvailable() = available

        override suspend fun getProducts(productIds: List<String>) = productIds.reversed().map {
            Product(it, it, "", "1", "IRR", ProductType.CONSUMABLE)
        }

        override suspend fun purchase(productId: String): ProductPurchaseOutcome {
            purchaseCalls++
            if (nextResult != PurchaseResult.SUCCESS) return ProductPurchaseOutcome(nextResult, errorMessage = "scripted")
            val p = OwnedPurchase(productId, "tok_${counter++}", "order", 0L, providerName)
            owned += p
            return ProductPurchaseOutcome(PurchaseResult.SUCCESS, p)
        }

        override suspend fun consume(purchaseToken: String): Boolean {
            if (!consumeWorks) return false
            owned.removeAll { it.purchaseToken == purchaseToken }
            return true
        }

        override suspend fun queryOwnedPurchases(): Result<List<OwnedPurchase>> =
            if (queryFails) Result.failure(IllegalStateException("offline")) else Result.success(owned.toList())
    }

    private class ScriptedVerifier : PurchaseVerifier {
        var result = VerificationResult.VERIFIED
        override suspend fun verifyProduct(productId: String, purchaseToken: String) = result
    }

    private class InMemoryPurchaseDao : PurchaseDao {
        val records = mutableMapOf<String, PurchaseRecordEntity>()
        override suspend fun getByToken(purchaseToken: String) = records[purchaseToken]
        override suspend fun save(record: PurchaseRecordEntity) {
            records[record.purchaseToken] = record
        }
    }

    private class CountingRewardEngine : RewardEngine(MonetizationArchitectureTest.mockPlayerDao()) {
        var calls = 0
        val coins = mutableListOf<Int>()
        override suspend fun applyReward(reward: Reward, source: RewardSource, playerId: String) {
            calls++
            if (reward.coins > 0) coins += reward.coins
        }
    }

    private object NoAnalytics : AnalyticsProvider {
        override fun trackEvent(eventName: String, params: Map<String, Any>) {}
        override fun trackPurchase(productId: String, amount: Double, currency: String) {}
        override fun trackAdEvent(placement: AdPlacement, result: AdResult) {}
    }

    private class InMemoryPlayerDao : PlayerDao {
        private val entitlements = MutableStateFlow<Map<EntitlementType, EntitlementEntity>>(emptyMap())
        fun isActive(type: EntitlementType) = entitlements.value[type]?.isActive == true
        fun clear() { entitlements.value = emptyMap() }

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
