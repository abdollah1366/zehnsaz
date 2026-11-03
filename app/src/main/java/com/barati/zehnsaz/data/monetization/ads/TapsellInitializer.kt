package com.barati.zehnsaz.data.monetization.ads

import android.app.Application
import android.util.Log
import com.barati.zehnsaz.BuildConfig
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks

/**
 * Initializes Tapsell Plus once, from Application.onCreate, as the Tapsell docs ask
 * (developer.tapsell.ir > SDK legacy Tapsell Plus > Android > initialize).
 */
object TapsellInitializer {
    private const val TAG = "TapsellInit"

    @Volatile
    var isInitialized: Boolean = false
        private set

    fun initialize(application: Application) {
        if (!TapsellConfig.isConfigured) return

        TapsellPlus.initialize(application, TapsellConfig.appKey, object : TapsellPlusInitListener {
            override fun onInitializeSuccess(adNetworks: AdNetworks) {
                isInitialized = true
                Log.d(TAG, "initialized: ${adNetworks.name}")
            }

            override fun onInitializeFailed(adNetworks: AdNetworks, adNetworkError: AdNetworkError) {
                Log.e(TAG, "init failed: ${adNetworks.name}, ${adNetworkError.errorMessage}")
            }
        })

        // false = no personalised ads. Users of a cognitive-health app should not be profiled for
        // ad targeting; change to true only with explicit user consent.
        TapsellPlus.setGDPRConsent(application, false)

        if (BuildConfig.DEBUG) TapsellPlus.setDebugMode(Log.DEBUG)
    }
}
