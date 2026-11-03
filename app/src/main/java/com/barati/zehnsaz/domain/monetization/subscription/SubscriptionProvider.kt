package com.barati.zehnsaz.domain.monetization.subscription

/**
 * A store that sells subscriptions (Cafe Bazaar today; Myket or a web gateway could be added
 * later by implementing this interface and binding it in MonetizationModule).
 */
interface SubscriptionProvider {
    /** Short stable name stored with the status, e.g. "bazaar". */
    val providerName: String

    /** False when the store app is missing or the provider is not configured. */
    fun isAvailable(): Boolean

    /** Store-side title and price for each plan id. Unknown ids are left out. */
    suspend fun getPlans(planIds: List<String>): List<SubscriptionPlan>

    /** Opens the store's payment flow for [planId]. Needs a foreground activity. */
    suspend fun subscribe(planId: String): SubscriptionPurchaseOutcome

    /**
     * Subscriptions the store reports as active right now.
     * Returns a failure when the store cannot be reached, so callers can keep the cached status.
     */
    suspend fun queryActiveSubscriptions(): Result<List<ActiveSubscription>>
}
