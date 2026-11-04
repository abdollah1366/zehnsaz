package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.data.local.dao.PurchaseDao
import com.barati.zehnsaz.data.local.entities.PurchaseRecordEntity
import com.barati.zehnsaz.domain.economy.RewardEngine
import com.barati.zehnsaz.domain.model.EntitlementType
import com.barati.zehnsaz.domain.model.OwnedPurchase
import com.barati.zehnsaz.domain.model.Product
import com.barati.zehnsaz.domain.model.PurchaseResult
import com.barati.zehnsaz.domain.model.Reward
import com.barati.zehnsaz.domain.model.RewardSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sells the in-app products in [ProductCatalog] and delivers each purchase exactly once.
 *
 * Delivery order for one purchase token: verify, give the reward or entitlement, record the token
 * as granted, then (consumables only) consume it in the store and record that. If the app dies
 * anywhere in between, [restorePurchases] picks the purchase up again from the store and finishes
 * the remaining steps without giving the reward twice.
 */
@Singleton
class ShopManager @Inject constructor(
    private val purchaseProvider: PurchaseProvider,
    private val purchaseDao: PurchaseDao,
    private val verifier: PurchaseVerifier,
    private val rewardEngine: RewardEngine,
    private val entitlementManager: EntitlementManager,
    private val analyticsProvider: AnalyticsProvider
) {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    /** One-time products this device owns (bought or restored), by product id. */
    val ownedProductIds: Flow<Set<String>> = entitlementManager.activeEntitlements().map { active ->
        ProductCatalog.entries.filter { it.entitlement != null && it.entitlement in active }.map { it.productId }.toSet()
    }

    /** True once lifetime premium has been bought (or restored) on this device. */
    val lifetimePremiumOwned: Flow<Boolean> = entitlementManager.hasEntitlement(EntitlementType.PREMIUM_LIFETIME)

    /** True when a one-time purchase (remove ads or lifetime premium) makes the app ad-free. */
    val adFreeOwned: Flow<Boolean> = entitlementManager.activeEntitlements().map { active ->
        active.any { it in ProductCatalog.AD_FREE_ENTITLEMENTS }
    }

    private val deliveryMutex = Mutex()

    data class RestoreResult(
        /** False when the store could not be reached; nothing was changed. */
        val reachedStore: Boolean,
        /** Purchases delivered by this restore (interrupted consumables and one-time products). */
        val restoredCount: Int
    )

    private enum class Delivery { NEW, ALREADY_DELIVERED, REJECTED, UNKNOWN_PRODUCT }

    fun isStoreAvailable(): Boolean = purchaseProvider.isAvailable()

    suspend fun refreshProducts() {
        if (!purchaseProvider.isAvailable()) {
            _products.value = emptyList()
            return
        }
        val fetched = runCatching { purchaseProvider.getProducts(ProductCatalog.productIds) }.getOrDefault(emptyList())
        // Keep the catalog's order; the store returns them in any order.
        val order = ProductCatalog.productIds.withIndex().associate { it.value to it.index }
        _products.value = fetched.sortedBy { order[it.productId] ?: Int.MAX_VALUE }
        analyticsProvider.trackEvent("shop_opened", mapOf("products" to fetched.size))
    }

    suspend fun purchase(productId: String): PurchaseResult {
        analyticsProvider.trackEvent("purchase_started", mapOf("productId" to productId))
        val result = runPurchase(productId)
        analyticsProvider.trackEvent("purchase_result", mapOf("productId" to productId, "result" to result.name))
        return result
    }

    private suspend fun runPurchase(productId: String): PurchaseResult {
        val entry = ProductCatalog.find(productId) ?: return PurchaseResult.FAILED
        if (!purchaseProvider.isAvailable()) return PurchaseResult.NOT_AVAILABLE
        if (entry.entitlement != null && hasEntitlement(entry.entitlement)) return PurchaseResult.ALREADY_OWNED

        val outcome = runCatching { purchaseProvider.purchase(productId) }
            .getOrElse { return PurchaseResult.FAILED }
        val purchase = outcome.purchase
        if (outcome.result != PurchaseResult.SUCCESS || purchase == null) {
            if (outcome.result == PurchaseResult.FAILED) {
                analyticsProvider.trackEvent(
                    "purchase_error",
                    mapOf("productId" to productId, "reason" to (outcome.errorMessage ?: "unknown"))
                )
                // Bazaar refuses to sell a consumable again while an earlier purchase of it is still
                // unconsumed (for example after a crash). Finishing that purchase unblocks the next try.
                restorePurchases()
            }
            return outcome.result
        }
        return when (deliver(purchase)) {
            Delivery.NEW, Delivery.ALREADY_DELIVERED -> PurchaseResult.SUCCESS
            Delivery.REJECTED -> PurchaseResult.VERIFICATION_FAILED
            Delivery.UNKNOWN_PRODUCT -> PurchaseResult.FAILED
        }
    }

    /**
     * Asks the store which purchases this account owns and delivers any that are not delivered yet.
     * Safe to call at any time and as often as needed (on app start, when the shop opens, and from
     * the "restore purchases" button).
     */
    suspend fun restorePurchases(): RestoreResult {
        if (!purchaseProvider.isAvailable()) return RestoreResult(reachedStore = false, restoredCount = 0)
        val owned = purchaseProvider.queryOwnedPurchases().getOrElse {
            analyticsProvider.trackEvent("purchase_restore_failed")
            return RestoreResult(reachedStore = false, restoredCount = 0)
        }
        var restored = 0
        for (purchase in owned) {
            if (deliver(purchase) == Delivery.NEW) restored++
        }
        analyticsProvider.trackEvent("purchase_restore", mapOf("owned" to owned.size, "restored" to restored))
        return RestoreResult(reachedStore = true, restoredCount = restored)
    }

    private suspend fun deliver(purchase: OwnedPurchase): Delivery = deliveryMutex.withLock { deliverLocked(purchase) }

    private suspend fun deliverLocked(purchase: OwnedPurchase): Delivery {
        val entry = ProductCatalog.find(purchase.productId)
        if (entry == null) {
            analyticsProvider.trackEvent("purchase_unknown_product", mapOf("productId" to purchase.productId))
            return Delivery.UNKNOWN_PRODUCT
        }

        val existing = purchaseDao.getByToken(purchase.purchaseToken)
        val record: PurchaseRecordEntity
        val delivery: Delivery

        if (existing != null && existing.granted) {
            record = existing
            delivery = Delivery.ALREADY_DELIVERED
            // The record survives, but make sure a one-time product's unlock does too.
            entry.entitlement?.let { if (!hasEntitlement(it)) entitlementManager.grantEntitlement(it) }
        } else {
            if (verifier.verifyProduct(purchase.productId, purchase.purchaseToken) == VerificationResult.REJECTED) {
                analyticsProvider.trackEvent("purchase_rejected", mapOf("productId" to purchase.productId))
                return Delivery.REJECTED
            }
            grant(entry.reward, entry.entitlement)
            record = PurchaseRecordEntity(
                purchaseToken = purchase.purchaseToken,
                productId = purchase.productId,
                orderId = purchase.orderId,
                provider = purchase.provider,
                purchaseTime = purchase.purchaseTime,
                granted = true,
                consumed = false
            )
            purchaseDao.save(record)
            delivery = Delivery.NEW
        }

        if (entry.kind == ProductCatalog.Kind.CONSUMABLE && !record.consumed) {
            // If this fails the purchase stays owned in Bazaar; the next restore consumes it
            // without giving the reward again, because the record says it was granted.
            if (purchaseProvider.consume(purchase.purchaseToken)) {
                purchaseDao.save(record.copy(consumed = true, updatedAt = System.currentTimeMillis()))
            } else {
                analyticsProvider.trackEvent("purchase_consume_failed", mapOf("productId" to purchase.productId))
            }
        }
        return delivery
    }

    private suspend fun grant(reward: Reward, entitlement: EntitlementType?) {
        if (reward.xp > 0 || reward.coins > 0 || reward.gems > 0 || reward.items.isNotEmpty()) {
            rewardEngine.applyReward(reward, RewardSource.SHOP)
        }
        entitlement?.let { entitlementManager.grantEntitlement(it) }
    }

    private suspend fun hasEntitlement(type: EntitlementType): Boolean =
        entitlementManager.hasEntitlement(type).first()
}
