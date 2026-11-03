package com.barati.zehnsaz.data.monetization.ads

import com.barati.zehnsaz.domain.model.AdPlacement
import com.barati.zehnsaz.domain.model.AdResult
import com.barati.zehnsaz.domain.model.AdStatus
import com.barati.zehnsaz.domain.monetization.RewardedAdProvider
import com.barati.zehnsaz.ui.common.ActivityProvider
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rewarded video ads through Tapsell Mediation.
 *
 * Flow: [load] requests an ad and keeps its id; [show] plays it. The reward is reported only from
 * `onRewarded` (the user watched to the end); closing early reports CANCELLED. After every show
 * the next ad is preloaded so it is ready by the time the cooldown in AdManager has passed.
 */
@Singleton
class ProductionRewardedAdProvider @Inject constructor(
    private val activityProvider: ActivityProvider
) : RewardedAdProvider {

    @Volatile private var status = AdStatus.IDLE
    @Volatile private var readyAdId: String? = null

    override fun isAvailable(): Boolean {
        if (!TapsellConfig.isConfigured) return false
        if (readyAdId == null && status != AdStatus.LOADING) load()
        return readyAdId != null
    }

    override fun load() {
        if (!TapsellConfig.isConfigured || status == AdStatus.LOADING || readyAdId != null) return
        status = AdStatus.LOADING
        Tapsell.requestRewardedAd(TapsellConfig.rewardedZoneId, object : RequestResultListener {
            override fun onSuccess(adId: String) {
                readyAdId = adId
                status = AdStatus.READY
            }

            override fun onFailure(message: String) {
                readyAdId = null
                status = AdStatus.FAILED
            }
        })
    }

    override fun show(placement: AdPlacement, onResult: (AdResult) -> Unit) {
        val adId = readyAdId
        if (adId == null) {
            load()
            onResult(AdResult.NOT_AVAILABLE)
            return
        }
        val activity = activityProvider.getActivity()
        if (activity == null) {
            onResult(AdResult.FAILED)
            return
        }

        // Each ad id can be shown once.
        readyAdId = null
        status = AdStatus.SHOWING
        var rewarded = false
        var reported = false
        fun report(result: AdResult) {
            if (reported) return
            reported = true
            onResult(result)
        }

        Tapsell.showRewardedAd(adId, activity, object : AdStateListener.Rewarded {
            override fun onAdImpression() {}
            override fun onAdClicked() {}

            override fun onRewarded() {
                // Report straight away: some networks fire onRewarded only after closing,
                // others before; reporting here works for both orders.
                rewarded = true
                report(AdResult.COMPLETED)
            }

            override fun onAdClosed(completionState: AdShowCompletionState) {
                if (!rewarded) report(AdResult.CANCELLED)
                status = AdStatus.IDLE
                load()
            }

            override fun onAdFailed(message: String) {
                status = AdStatus.FAILED
                report(AdResult.FAILED)
                status = AdStatus.IDLE
                load()
            }
        })
    }

    override fun getStatus(): AdStatus = status
}
