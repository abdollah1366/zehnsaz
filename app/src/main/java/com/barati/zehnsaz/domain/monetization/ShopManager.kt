package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.di.MainDispatcher
import com.barati.zehnsaz.domain.economy.RewardEngine
import com.barati.zehnsaz.domain.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopManager @Inject constructor(
    private val purchaseProvider: PurchaseProvider,
    private val rewardEngine: RewardEngine,
    private val entitlementManager: EntitlementManager,
    private val analyticsProvider: AnalyticsProvider,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()
    
    private val scope = CoroutineScope(mainDispatcher)

    suspend fun refreshProducts() {
        if (purchaseProvider.isAvailable()) {
            val fetched = purchaseProvider.getProducts(MonetizationConfig.PRODUCT_IDS)
            _products.value = fetched
            analyticsProvider.trackEvent("shop_opened")
        }
    }

    fun purchaseProduct(productId: String, onResult: (PurchaseResult) -> Unit) {
        analyticsProvider.trackEvent("purchase_started", mapOf("productId" to productId))
        
        purchaseProvider.purchase(productId) { result ->
            if (result == PurchaseResult.SUCCESS) {
                handleSuccessfulPurchase(productId)
            }
            analyticsProvider.trackEvent("purchase_result", mapOf("productId" to productId, "result" to result.name))
            onResult(result)
        }
    }

    fun restorePurchases(onResult: (Boolean) -> Unit) {
        purchaseProvider.restorePurchases { ownedProductIds ->
            scope.launch {
                ownedProductIds.forEach { id ->
                    grantEntitlementForProduct(id)
                }
                onResult(true)
            }
        }
    }

    private fun handleSuccessfulPurchase(productId: String) {
        // Grant Rewards (Consumables)
        val reward = getRewardForProduct(productId)
        if (reward.xp > 0 || reward.coins > 0 || reward.gems > 0 || reward.items.isNotEmpty()) {
            scope.launch {
                rewardEngine.applyReward(reward, RewardSource.SHOP)
            }
        }

        // Grant Entitlements (Non-consumables)
        grantEntitlementForProduct(productId)
    }

    private fun grantEntitlementForProduct(productId: String) {
        val entitlement = when (productId) {
            "remove_ads" -> EntitlementType.REMOVE_ADS
            "premium_training_pack" -> EntitlementType.PREMIUM_TRAINING
            "level_pack_01" -> EntitlementType.LEVEL_PACK_01
            else -> null
        }

        entitlement?.let {
            scope.launch {
                entitlementManager.grantEntitlement(it)
            }
        }
    }

    private fun getRewardForProduct(productId: String): Reward {
        return when (productId) {
            "coins_100" -> Reward(coins = 100, xp = 10)
            "coins_500" -> Reward(coins = 500, xp = 50)
            "coins_1200" -> Reward(coins = 1200, xp = 150)
            "gems_10" -> Reward(gems = 10, xp = 20)
            "gems_50" -> Reward(gems = 50, xp = 100)
            "gems_150" -> Reward(gems = 150, xp = 400)
            "booster_pack_small" -> Reward(items = mapOf(ItemType.HINT to 2, ItemType.UNDO to 2))
            "booster_pack_large" -> Reward(items = mapOf(ItemType.HINT to 10, ItemType.UNDO to 10, ItemType.SHUFFLE to 5))
            "remove_ads" -> Reward(xp = 100)
            "premium_training_pack" -> Reward(xp = 500)
            "level_pack_01" -> Reward(xp = 200)
            else -> Reward()
        }
    }
}
