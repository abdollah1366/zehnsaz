package com.example.zehnchin.data.monetization.ads

import android.content.Context
import com.example.zehnchin.domain.model.AdPlacement
import com.example.zehnchin.domain.model.AdResult
import com.example.zehnchin.domain.model.AdStatus
import com.example.zehnchin.domain.monetization.RewardedAdProvider
import com.example.zehnchin.ui.common.ActivityProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Placeholder for the real advertising SDK integration.
 * Currently stubbed to allow compilation.
 */
@Singleton
class ProductionRewardedAdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityProvider: ActivityProvider
) : RewardedAdProvider {
    
    private var status = AdStatus.IDLE

    override fun isAvailable(): Boolean = false

    override fun load() {
        status = AdStatus.LOADING
        // Simulation of loading failure for now as it's a placeholder
        status = AdStatus.FAILED
    }

    override fun show(placement: AdPlacement, onResult: (AdResult) -> Unit) {
        onResult(AdResult.NOT_AVAILABLE)
    }

    override fun getStatus(): AdStatus = status
}
