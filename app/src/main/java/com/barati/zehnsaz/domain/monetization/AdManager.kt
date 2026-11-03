package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.data.local.dao.PlayerDao
import com.barati.zehnsaz.di.MainDispatcher
import com.barati.zehnsaz.domain.economy.BalanceConfig
import com.barati.zehnsaz.domain.economy.RewardEngine
import com.barati.zehnsaz.domain.model.*
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ad policy for Zehnsaz. The app is used by patients and is recommended by clinicians, so ads
 * must never get in the way of training or assessment:
 *
 * - Only rewarded video, and only when the user taps a button that says it is an ad and states
 *   the reward. No interstitials, no banners, no ads that open by themselves.
 * - Never during a task or an assessment; offered only on the result screen and the failure
 *   dialog, after the trial data has been recorded.
 * - "Skip a level with an ad" only after [MonetizationConfig.FAILURES_BEFORE_AD_SKIP] failures in
 *   a row, so ads don't replace practice at the right difficulty.
 * - The reward is granted only after the video was watched to the end.
 * - Daily cap and cooldown below; subscribers never see ads.
 */
@Singleton
class AdManager @Inject constructor(
    private val adProvider: RewardedAdProvider,
    private val rewardEngine: RewardEngine,
    private val playerDao: PlayerDao,
    private val analyticsProvider: AnalyticsProvider,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val subscriptionState: SubscriptionState = SubscriptionState()
) {
    private val scope = CoroutineScope(mainDispatcher)
    
    private var lastAdTimestamp: Long = 0
    private var adsWatchedToday: Int = 0
    private var lastDayOfYear: Int = -1

    // Consecutive failures per level ("chapter:level"), kept for this app session only.
    private val consecutiveFailures = mutableMapOf<String, Int>()

    fun onLevelFailed(levelKey: String) {
        consecutiveFailures[levelKey] = (consecutiveFailures[levelKey] ?: 0) + 1
    }

    fun onLevelPassed(levelKey: String) {
        consecutiveFailures.remove(levelKey)
    }

    /** Whether the failure dialog may offer "skip this level by watching an ad". */
    fun canOfferLevelSkip(levelKey: String): Boolean =
        (consecutiveFailures[levelKey] ?: 0) >= MonetizationConfig.FAILURES_BEFORE_AD_SKIP && canShowAd()

    fun canShowAd(): Boolean {
        // Subscribers get an ad-free app.
        if (subscriptionState.isSubscribed) return false
        if (!adProvider.isAvailable()) return false
        
        val now = System.currentTimeMillis()
        
        // Cooldown check
        if (now - lastAdTimestamp < MonetizationConfig.REWARDED_AD_COOLDOWN_SECONDS * 1000L) {
            return false
        }
        
        // Daily limit check
        resetDailyLimitIfNewDay()
        if (adsWatchedToday >= MonetizationConfig.REWARDED_AD_DAILY_LIMIT) {
            return false
        }
        
        return true
    }

    /** Starts loading an ad in the background so it is ready when the user taps "watch". */
    fun preload() {
        if (!subscriptionState.isSubscribed) adProvider.load()
    }

    fun showRewardedAd(placement: AdPlacement, onResult: (AdResult) -> Unit) {
        analyticsProvider.trackEvent("AD_OFFERED", mapOf("placement" to placement.name))
        
        if (!canShowAd()) {
            analyticsProvider.trackEvent("AD_FAILED", mapOf("placement" to placement.name, "reason" to "LIMIT_OR_COOLDOWN"))
            onResult(AdResult.NOT_AVAILABLE)
            return
        }

        analyticsProvider.trackEvent("AD_STARTED", mapOf("placement" to placement.name))
        
        val resultProcessed = AtomicBoolean(false)

        adProvider.show(placement) { result ->
            if (resultProcessed.compareAndSet(false, true)) {
                if (result == AdResult.COMPLETED) {
                    adsWatchedToday++
                    lastAdTimestamp = System.currentTimeMillis()
                    grantAdReward(placement)
                    analyticsProvider.trackEvent("AD_COMPLETED", mapOf("placement" to placement.name))
                } else {
                    analyticsProvider.trackEvent("AD_FAILED", mapOf("placement" to placement.name, "result" to result.name))
                }
                
                analyticsProvider.trackAdEvent(placement, result)
                onResult(result)
            }
        }
    }

    private fun resetDailyLimitIfNewDay() {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        if (currentDay != lastDayOfYear) {
            adsWatchedToday = 0
            lastDayOfYear = currentDay
        }
    }

    private fun grantAdReward(placement: AdPlacement) {
        val reward = BalanceConfig.getAdReward(placement)
        scope.launch {
            rewardEngine.applyReward(reward, RewardSource.BONUS)
            analyticsProvider.trackEvent("AD_REWARD_GRANTED", mapOf("placement" to placement.name))
        }
    }
}
