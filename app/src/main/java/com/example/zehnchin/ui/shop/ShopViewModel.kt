package com.example.zehnchin.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.domain.model.AdPlacement
import com.example.zehnchin.domain.model.AdResult
import com.example.zehnchin.domain.monetization.AdManager
import com.example.zehnchin.domain.monetization.ShopManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopManager: ShopManager,
    private val adManager: AdManager
) : ViewModel() {

    val products = shopManager.products

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing = _isPurchasing.asStateFlow()

    private val _message = MutableStateFlow<Int?>(null)
    val message = _message.asStateFlow()

    init {
        viewModelScope.launch {
            shopManager.refreshProducts()
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
                _message.value = com.example.zehnchin.R.string.ad_reward_success
            } else if (result == AdResult.NOT_AVAILABLE) {
                _message.value = com.example.zehnchin.R.string.ad_not_available
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
