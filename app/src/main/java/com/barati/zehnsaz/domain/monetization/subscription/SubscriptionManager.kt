package com.barati.zehnsaz.domain.monetization.subscription

import com.barati.zehnsaz.domain.model.EntitlementType
import com.barati.zehnsaz.domain.model.PurchaseResult
import com.barati.zehnsaz.domain.monetization.AnalyticsProvider
import com.barati.zehnsaz.domain.monetization.EntitlementManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps [SubscriptionState] in step with the store.
 *
 * - On start it seeds the state from the local entitlement cache, so a subscriber who opens the
 *   app offline still has premium access.
 * - [refresh] asks the store; a successful answer overwrites the cache, a failed one keeps it.
 * - [subscribe] runs the store's payment flow and, on success, activates immediately.
 */
@Singleton
class SubscriptionManager @Inject constructor(
    private val provider: SubscriptionProvider,
    private val entitlementManager: EntitlementManager,
    private val state: SubscriptionState,
    private val analyticsProvider: AnalyticsProvider
) {
    val status: StateFlow<SubscriptionStatus> = state.status

    private val _plans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val plans: StateFlow<List<SubscriptionPlan>> = _plans.asStateFlow()

    suspend fun loadCachedStatus() {
        val cached = entitlementManager.hasEntitlement(EntitlementType.PREMIUM_SUBSCRIPTION).first()
        if (!state.status.value.isVerifiedNow) {
            state.update(SubscriptionStatus(isActive = cached, isVerifiedNow = false))
        }
    }

    suspend fun refresh() {
        if (!provider.isAvailable()) return
        provider.queryActiveSubscriptions()
            // Every subscription Bazaar returns belongs to this app, so any active one unlocks premium.
            .onSuccess { active -> applyActive(active.firstOrNull()) }
            .onFailure { analyticsProvider.trackEvent("subscription_refresh_failed") }
    }

    suspend fun refreshPlans() {
        if (!provider.isAvailable()) return
        val fetched = runCatching { provider.getPlans(SubscriptionCatalog.planIds) }.getOrDefault(emptyList())
        _plans.value = fetched.sortedBy { it.period.ordinal }
    }

    suspend fun subscribe(planId: String): SubscriptionPurchaseOutcome {
        analyticsProvider.trackEvent("subscription_started", mapOf("planId" to planId))
        if (!provider.isAvailable()) {
            return SubscriptionPurchaseOutcome(PurchaseResult.NOT_AVAILABLE)
        }
        if (state.isSubscribed) {
            return SubscriptionPurchaseOutcome(PurchaseResult.ALREADY_OWNED)
        }
        val outcome = provider.subscribe(planId)
        if (outcome.result == PurchaseResult.SUCCESS && outcome.subscription != null) {
            applyActive(outcome.subscription)
        }
        analyticsProvider.trackEvent(
            "subscription_result",
            mapOf("planId" to planId, "result" to outcome.result.name)
        )
        return outcome
    }

    private suspend fun applyActive(subscription: ActiveSubscription?) {
        if (subscription != null) {
            entitlementManager.grantEntitlement(EntitlementType.PREMIUM_SUBSCRIPTION)
            state.update(
                SubscriptionStatus(
                    isActive = true,
                    planId = subscription.planId,
                    provider = subscription.provider,
                    isVerifiedNow = true
                )
            )
        } else {
            entitlementManager.revokeEntitlement(EntitlementType.PREMIUM_SUBSCRIPTION)
            state.update(SubscriptionStatus(isActive = false, isVerifiedNow = true))
        }
    }
}
