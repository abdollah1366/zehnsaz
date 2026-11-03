package com.barati.zehnsaz.data.monetization

import com.barati.zehnsaz.domain.model.PurchaseResult
import com.barati.zehnsaz.domain.monetization.subscription.ActiveSubscription
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionCatalog
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionPeriod
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionPlan
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionProvider
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionPurchaseOutcome
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Used in debug builds when Bazaar is not configured, so the shop and premium flow can be
 * tried on an emulator without the Bazaar app. Never bound in release builds.
 */
@Singleton
class FakeSubscriptionProvider @Inject constructor() : SubscriptionProvider {
    override val providerName = "fake"
    private var active: ActiveSubscription? = null

    override fun isAvailable() = true

    override suspend fun getPlans(planIds: List<String>): List<SubscriptionPlan> {
        val ids = planIds.ifEmpty { listOf("fake_monthly", "fake_yearly") }
        return ids.map { id ->
            val period = if (id == "fake_yearly") SubscriptionPeriod.YEARLY else SubscriptionCatalog.periodOf(id)
            SubscriptionPlan(id, period, title = id, description = "اشتراک آزمایشی", price = "۰ تومان")
        }
    }

    override suspend fun subscribe(planId: String): SubscriptionPurchaseOutcome {
        val sub = ActiveSubscription(planId, "fake_token", "fake_order", System.currentTimeMillis(), providerName)
        active = sub
        return SubscriptionPurchaseOutcome(PurchaseResult.SUCCESS, sub)
    }

    override suspend fun queryActiveSubscriptions() = Result.success(listOfNotNull(active))
}
