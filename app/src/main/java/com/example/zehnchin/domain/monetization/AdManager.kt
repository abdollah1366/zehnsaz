package com.example.zehnchin.domain.monetization

import com.example.zehnchin.data.local.dao.PlayerDao
import com.example.zehnchin.di.MainDispatcher
import com.example.zehnchin.domain.economy.BalanceConfig
import com.example.zehnchin.domain.economy.RewardEngine
import com.example.zehnchin.domain.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    private val adProvider: RewardedAdProvider,
    private val rewardEngine: RewardEngine,
    private val playerDao: PlayerDao,
    private val analyticsProvider: AnalyticsProvider,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private val scope = CoroutineScope(mainDispatcher)
    
    private var lastAdTimestamp: Long = 0
    private var adsWatchedToday: Int = 0
    private var lastDayOfYear: Int = -1

    fun canShowAd(): Boolean {
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
