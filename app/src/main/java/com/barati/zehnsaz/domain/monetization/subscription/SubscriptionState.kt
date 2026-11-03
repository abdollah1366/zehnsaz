package com.barati.zehnsaz.domain.monetization.subscription

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for "is this user a subscriber?", shared by every feature that
 * cares (ads, premium content, the shop). Only [SubscriptionManager] writes to it.
 */
@Singleton
class SubscriptionState @Inject constructor() {
    private val _status = MutableStateFlow(SubscriptionStatus.NONE)
    val status: StateFlow<SubscriptionStatus> = _status.asStateFlow()

    val isSubscribed: Boolean get() = _status.value.isActive

    internal fun update(status: SubscriptionStatus) {
        _status.value = status
    }
}
