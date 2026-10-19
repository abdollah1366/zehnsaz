package com.example.zehnchin.data.monetization

import android.content.Context
import com.example.zehnchin.R
import com.example.zehnchin.domain.model.*
import com.example.zehnchin.domain.monetization.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePurchaseProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchaseProvider {
    override suspend fun getProducts(productIds: List<String>): List<Product> {
        delay(500)
        return productIds.map { id ->
            val titleRes = when (id) {
                "coins_100" -> R.string.product_coins_100
                "coins_500" -> R.string.product_coins_500
                "coins_1200" -> R.string.product_coins_1200
                "gems_10" -> R.string.product_gems_10
                "gems_50" -> R.string.product_gems_50
                "gems_150" -> R.string.product_gems_150
                "booster_pack_small" -> R.string.product_boosters_small
                "booster_pack_large" -> R.string.product_boosters_large
                "remove_ads" -> R.string.product_remove_ads
                "premium_training_pack" -> R.string.product_premium_pack
                "level_pack_01" -> R.string.product_level_pack_01
                else -> R.string.product_test
            }
            
            val title = if (titleRes == R.string.product_test) {
                context.getString(titleRes, id)
            } else {
                context.getString(titleRes)
            }

            Product(
                productId = id,
                title = title,
                description = "توضیحات مربوط به $title",
                price = "۵۰,۰۰۰ تومان",
                currency = "IRT",
                productType = when {
                    id.contains("coin") || id.contains("gem") -> ProductType.CONSUMABLE
                    id.contains("booster") -> ProductType.BOOSTER_PACK
                    else -> ProductType.NON_CONSUMABLE
                },
                isAvailable = true
            )
        }
    }

    override fun purchase(productId: String, onResult: (PurchaseResult) -> Unit) {
        // Simple success for fake provider
        onResult(PurchaseResult.SUCCESS)
    }

    override fun restorePurchases(onResult: (List<String>) -> Unit) {
        // Mock restore of non-consumables if they were "purchased" before
        onResult(emptyList()) 
    }

    override fun isAvailable(): Boolean = true
}

@Singleton
class FakeRewardedAdProvider @Inject constructor() : RewardedAdProvider {
    private var status = AdStatus.IDLE

    override fun isAvailable(): Boolean = true
    
    override fun load() {
        status = AdStatus.LOADING
        // Simulation delay would be here
        status = AdStatus.READY
    }

    override fun show(placement: AdPlacement, onResult: (AdResult) -> Unit) {
        status = AdStatus.SHOWING
        // Return COMPLETED after a short delay simulation
        onResult(AdResult.COMPLETED)
        status = AdStatus.COMPLETED
    }

    override fun getStatus(): AdStatus = status
}

@Singleton
class FakeInterstitialAdProvider @Inject constructor() : InterstitialAdProvider {
    override fun isAvailable(): Boolean = true
    override fun load() {}
    override fun show(onResult: (AdResult) -> Unit) {
        onResult(AdResult.COMPLETED)
    }
}

@Singleton
class FakeAnalyticsProvider @Inject constructor() : AnalyticsProvider {
    override fun trackEvent(eventName: String, params: Map<String, Any>) {
        println("Analytics Event: $eventName, Params: $params")
    }

    override fun trackPurchase(productId: String, amount: Double, currency: String) {
        println("Analytics Purchase: $productId, Amount: $amount $currency")
    }

    override fun trackAdEvent(placement: AdPlacement, result: AdResult) {
        println("Analytics Ad: $placement, Result: $result")
    }
}
