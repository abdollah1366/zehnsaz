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

interface PurchaseProvider {
    suspend fun getProducts(productIds: List<String>): List<Product>
    fun purchase(productId: String, onResult: (PurchaseResult) -> Unit)
    fun restorePurchases(onResult: (List<String>) -> Unit)
    fun isAvailable(): Boolean
}

interface AnalyticsProvider {
    fun trackEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun trackPurchase(productId: String, amount: Double, currency: String)
    fun trackAdEvent(placement: AdPlacement, result: AdResult)
}
