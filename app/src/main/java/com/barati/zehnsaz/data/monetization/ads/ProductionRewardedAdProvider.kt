package com.barati.zehnsaz.data.monetization.ads

import com.barati.zehnsaz.domain.model.AdPlacement
import com.barati.zehnsaz.domain.model.AdResult
import com.barati.zehnsaz.domain.model.AdStatus
import com.barati.zehnsaz.domain.monetization.RewardedAdProvider
import com.barati.zehnsaz.ui.common.ActivityProvider
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rewarded video ads through Tapsell Plus.
 *
 * Flow: [load] calls requestRewardedVideoAd and keeps the responseId; [show] calls
 * showRewardedVideoAd with it. The reward is reported only from onRewarded (the user watched to
 * the end); closing early reports CANCELLED. After every show the next ad is preloaded so it is
 * ready by the time the cooldown in AdManager has passed.
 */
@Singleton
class ProductionRewardedAdProvider @Inject constructor(
    private val activityProvider: ActivityProvider
) : RewardedAdProvider {

    @Volatile private var status = AdStatus.IDLE
    @Volatile private var responseId: String? = null

    override fun isAvailable(): Boolean {
        if (!TapsellConfig.isConfigured) return false
        if (responseId == null && status != AdStatus.LOADING) load()
        return responseId != null
    }

    override fun load() {
        if (!TapsellConfig.isConfigured || status == AdStatus.LOADING || responseId != null) return
        // Tapsell Plus needs an Activity to request an ad.
        val activity = activityProvider.getActivity() ?: return
        status = AdStatus.LOADING
        TapsellPlus.requestRewardedVideoAd(activity, TapsellConfig.rewardedZoneId, object : AdRequestCallback() {
            override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                super.response(tapsellPlusAdModel)
                responseId = tapsellPlusAdModel.responseId
                status = AdStatus.READY
            }

            override fun error(message: String) {
                super.error(message)
                responseId = null
                status = AdStatus.FAILED
            }
        })
    }

    override fun show(placement: AdPlacement, onResult: (AdResult) -> Unit) {
        val id = responseId
        if (id == null) {
            load()
            onResult(AdResult.NOT_AVAILABLE)
            return
        }
        val activity = activityProvider.getActivity()
        if (activity == null) {
            onResult(AdResult.FAILED)
            return
        }

        // Each responseId can be shown once.
        responseId = null
        status = AdStatus.SHOWING
        var rewarded = false
        var reported = false
        fun report(result: AdResult) {
            if (reported) return
            reported = true
            onResult(result)
        }

        TapsellPlus.showRewardedVideoAd(activity, id, object : AdShowListener() {
            override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                super.onOpened(tapsellPlusAdModel)
            }

            override fun onRewarded(tapsellPlusAdModel: TapsellPlusAdModel) {
                super.onRewarded(tapsellPlusAdModel)
                // Report straight away, whether onRewarded arrives before or after onClosed.
                rewarded = true
                report(AdResult.COMPLETED)
            }

            override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                super.onClosed(tapsellPlusAdModel)
                if (!rewarded) report(AdResult.CANCELLED)
                status = AdStatus.IDLE
                load()
            }

            override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                super.onError(tapsellPlusErrorModel)
                status = AdStatus.FAILED
                report(AdResult.FAILED)
                status = AdStatus.IDLE
                load()
            }
        })
    }

    override fun getStatus(): AdStatus = status
}
