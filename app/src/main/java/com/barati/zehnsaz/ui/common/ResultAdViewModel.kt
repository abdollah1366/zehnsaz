package com.barati.zehnsaz.ui.common

import androidx.lifecycle.ViewModel
import com.barati.zehnsaz.domain.economy.BalanceConfig
import com.barati.zehnsaz.domain.model.AdPlacement
import com.barati.zehnsaz.domain.model.AdResult
import com.barati.zehnsaz.domain.monetization.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * The optional "watch an ad for bonus coins" offer on a level's result screen. It appears only
 * after the level is finished and its data saved, only if an ad is ready, and only once per
 * result screen. Tapping it is the only way an ad is shown.
 */
@HiltViewModel
class ResultAdViewModel @Inject constructor(
    private val adManager: AdManager
) : ViewModel() {

    enum class OfferState { HIDDEN, OFFERED, SHOWING, CLAIMED, UNAVAILABLE }

    val bonusCoins: Int = BalanceConfig.getAdReward(AdPlacement.LEVEL_COMPLETED).coins

    private val _state = MutableStateFlow(
        if (adManager.canShowAd()) OfferState.OFFERED else OfferState.HIDDEN
    )
    val state: StateFlow<OfferState> = _state.asStateFlow()

    fun watchForBonus() {
        if (_state.value != OfferState.OFFERED) return
        _state.value = OfferState.SHOWING
        adManager.showRewardedAd(AdPlacement.LEVEL_COMPLETED) { result ->
            _state.value = when (result) {
                AdResult.COMPLETED -> OfferState.CLAIMED
                AdResult.CANCELLED -> OfferState.HIDDEN // the user chose not to; don't ask again
                else -> OfferState.UNAVAILABLE
            }
        }
    }
}
