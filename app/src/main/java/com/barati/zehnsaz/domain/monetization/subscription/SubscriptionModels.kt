package com.barati.zehnsaz.domain.monetization.subscription

import com.barati.zehnsaz.domain.model.PurchaseResult

/** How long one billing cycle of a plan lasts. Used for display and ordering only. */
enum class SubscriptionPeriod { MONTHLY, QUARTERLY, YEARLY }

/** A subscription plan as the store shows it (title and price come from the store). */
data class SubscriptionPlan(
    val planId: String,
    val period: SubscriptionPeriod,
    val title: String,
    val description: String,
    val price: String
)

/** A subscription the store says the user currently holds. */
data class ActiveSubscription(
    val planId: String,
    val purchaseToken: String,
    val orderId: String,
    val purchaseTime: Long,
    val provider: String
)

/**
 * The app-wide view of the user's subscription, independent of any store.
 *
 * [isVerifiedNow] is false when the value comes from the offline cache (the store could not be
 * reached), so the UI can say "last confirmed" instead of claiming a fresh check.
 */
data class SubscriptionStatus(
    val isActive: Boolean = false,
    val planId: String? = null,
    val provider: String? = null,
    val isVerifiedNow: Boolean = false
) {
    companion object {
        val NONE = SubscriptionStatus()
    }
}

data class SubscriptionPurchaseOutcome(
    val result: PurchaseResult,
    val subscription: ActiveSubscription? = null,
    val errorMessage: String? = null
)
