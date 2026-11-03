package com.barati.zehnsaz.data.monetization.ads

import android.content.Context
import com.barati.zehnsaz.domain.model.AdPlacement
import com.barati.zehnsaz.domain.model.AdResult
import com.barati.zehnsaz.domain.model.AdStatus
import com.barati.zehnsaz.domain.monetization.RewardedAdProvider
import com.barati.zehnsaz.ui.common.ActivityProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.tapsell.mediation.TapsellMediation
import ir.tapsell.mediation.ad.TapsellMediationRewardedVideoAd
import ir.tapsell.mediation.ad.TapsellMediationRewardedVideoAdListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductionRewardedAdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityProvider: ActivityProvider
) : RewardedAdProvider {
    
    private var status = AdStatus.IDLE
    private var loadedAd: TapsellMediationRewardedVideoAd? = null
    private var currentOnResult: ((AdResult) -> Unit)? = null

    init {
        TapsellMediation.initialize(context, TapsellConfig.getAppKey())
    }

    override fun isAvailable(): Boolean {
        return loadedAd != null && status == AdStatus.READY
    }

    override fun load() {
        if (status == AdStatus.LOADING) return
        
        status = AdStatus.LOADING
        TapsellMediation.requestRewardedVideoAd(context, TapsellConfig.getPlacementId("REWARDED"), object : TapsellMediationRewardedVideoAdListener {
            override fun onAdLoaded(ad: TapsellMediationRewardedVideoAd) {
                loadedAd = ad
                status = AdStatus.READY
            }

            override fun onAdFailed(error: String) {
                status = AdStatus.FAILED
                loadedAd = null
            }

            override fun onAdClosed(ad: TapsellMediationRewardedVideoAd) {
                status = AdStatus.IDLE
                loadedAd = null
                // Reward is handled in onRewarded, but we might want to signal "Done" here if not rewarded
                currentOnResult?.invoke(AdResult.CANCELLED)
                currentOnResult = null
                load() // Auto reload
            }

            override fun onRewarded(ad: TapsellMediationRewardedVideoAd) {
                currentOnResult?.invoke(AdResult.COMPLETED)
                currentOnResult = null
            }

            override fun onAdClicked(ad: TapsellMediationRewardedVideoAd) {}
        })
    }

    override fun show(placement: AdPlacement, onResult: (AdResult) -> Unit) {
        val activity = activityProvider.getActivity()
        if (activity == null) {
            onResult(AdResult.FAILED)
            return
        }

        val ad = loadedAd
        if (ad == null) {
            onResult(AdResult.NOT_AVAILABLE)
            load()
            return
        }

        currentOnResult = onResult
        status = AdStatus.SHOWING
        ad.show(activity)
    }

    override fun getStatus(): AdStatus = status
}
