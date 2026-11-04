package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.domain.model.*

interface RewardedAdProvider {
    fun isAvailable(): Boolean
    fun load()
    fun show(placement: AdPlacement, onResult: (AdResult) -> Unit)
    fun getStatus(): AdStatus
}

interface InterstitialAdProvider {
    fun isAvailable(): Boolean
    fun load()
    fun show(onResult: (AdResult) -> Unit)
}

/**
 * A store that sells one-time in-app products (Cafe Bazaar today).
 * Subscriptions have their own interface, see subscription/SubscriptionProvider.
 */
interface PurchaseProvider {
    /** Short stable name stored with each purchase, e.g. "bazaar". */
    val providerName: String

    /** False when the store app is missing or the provider is not configured. */
    fun isAvailable(): Boolean

    /** Store-side title and price for each product id. Unknown ids are left out. */
    suspend fun getProducts(productIds: List<String>): List<Product>

    /** Opens the store's payment flow for [productId]. Needs a foreground activity. */
    suspend fun purchase(productId: String): ProductPurchaseOutcome

    /** Consumes a consumable so it can be bought again. Returns false if the store could not be reached. */
    suspend fun consume(purchaseToken: String): Boolean

    /**
     * Products the store says this account owns and has not consumed: one-time products, plus any
     * consumable whose delivery was interrupted. Returns a failure when the store cannot be reached.
     */
    suspend fun queryOwnedPurchases(): Result<List<OwnedPurchase>>
}

interface AnalyticsProvider {
    fun trackEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun trackPurchase(productId: String, amount: Double, currency: String)
    fun trackAdEvent(placement: AdPlacement, result: AdResult)
}
