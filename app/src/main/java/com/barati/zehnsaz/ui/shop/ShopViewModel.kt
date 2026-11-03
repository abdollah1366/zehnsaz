package com.barati.zehnsaz.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barati.zehnsaz.domain.model.AdPlacement
import com.barati.zehnsaz.domain.model.AdResult
import com.barati.zehnsaz.domain.monetization.AdManager
import com.barati.zehnsaz.domain.model.PurchaseResult
import com.barati.zehnsaz.domain.monetization.ShopManager
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionManager
import com.barati.zehnsaz.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopManager: ShopManager,
    private val adManager: AdManager,
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {

    val products = shopManager.products
    val subscriptionPlans = subscriptionManager.plans
    val subscriptionStatus = subscriptionManager.status

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing = _isPurchasing.asStateFlow()

    private val _message = MutableStateFlow<Int?>(null)
    val message = _message.asStateFlow()

    init {
        adManager.preload()
        viewModelScope.launch {
            shopManager.refreshProducts()
        }
        viewModelScope.launch {
            subscriptionManager.refreshPlans()
            subscriptionManager.refresh()
        }
    }

    fun subscribe(planId: String) {
        if (_isPurchasing.value) return
        _isPurchasing.value = true
        viewModelScope.launch {
            val outcome = subscriptionManager.subscribe(planId)
            _isPurchasing.value = false
            _message.value = when (outcome.result) {
                PurchaseResult.SUCCESS -> R.string.subscription_success
                PurchaseResult.CANCELLED -> R.string.subscription_cancelled
                PurchaseResult.ALREADY_OWNED -> R.string.subscription_already_active
                PurchaseResult.NOT_AVAILABLE -> R.string.subscription_store_unavailable
                else -> R.string.subscription_failed
            }
        }
    }

    fun restoreSubscription() {
        viewModelScope.launch {
            subscriptionManager.refresh()
            _message.value = if (subscriptionManager.status.value.isActive) {
                R.string.subscription_restored
            } else {
                R.string.subscription_none_found
            }
        }
    }

    fun purchase(productId: String) {
        _isPurchasing.value = true
        shopManager.purchaseProduct(productId) {
            _isPurchasing.value = false
        }
    }

    fun watchAd() {
        adManager.showRewardedAd(AdPlacement.SHOP) { result ->
            if (result == AdResult.COMPLETED) {
                _message.value = R.string.ad_reward_success
            } else if (result == AdResult.NOT_AVAILABLE) {
                _message.value = R.string.ad_not_available
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
