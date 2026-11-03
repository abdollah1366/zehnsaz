package com.barati.zehnsaz.domain.monetization.subscription

import com.barati.zehnsaz.BuildConfig

/**
 * Subscription plan ids as defined in the Cafe Bazaar developer panel
 * (In-app products > Subscriptions). The ids come from local.properties / gradle.properties
 * through BuildConfig, see app/build.gradle.kts. An empty id disables that plan.
 */
object SubscriptionCatalog {
    private val byPeriod: Map<SubscriptionPeriod, String> = mapOf(
        SubscriptionPeriod.MONTHLY to BuildConfig.BAZAAR_SUB_MONTHLY,
        SubscriptionPeriod.QUARTERLY to BuildConfig.BAZAAR_SUB_QUARTERLY,
        SubscriptionPeriod.YEARLY to BuildConfig.BAZAAR_SUB_YEARLY
    ).filterValues { it.isNotBlank() }

    val planIds: List<String> get() = byPeriod.values.toList()

    fun periodOf(planId: String): SubscriptionPeriod =
        byPeriod.entries.firstOrNull { it.value == planId }?.key ?: SubscriptionPeriod.MONTHLY
}
