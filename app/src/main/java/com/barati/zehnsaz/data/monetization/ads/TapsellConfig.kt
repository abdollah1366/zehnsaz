package com.barati.zehnsaz.data.monetization.ads

import com.barati.zehnsaz.BuildConfig

/**
 * Tapsell Plus settings. Both values come from local.properties or gradle.properties through
 * app/build.gradle.kts. Debug builds fall back to the public test key and test rewarded zone from
 * Tapsell's official sample (TapsellPlusSDK-Jetpack-Compose-Sample), so ads can be tried before
 * the real ones exist. Release builds without a key show no ads.
 */
object TapsellConfig {
    const val TEST_APP_KEY = "alsoatsrtrotpqacegkehkaiieckldhrgsbspqtgqnbrrfccrtbdomgjtahflchkqtqosa"
    const val TEST_REWARDED_ZONE = "5cfaa802e8d17f0001ffb28e"

    val appKey: String
        get() = BuildConfig.TAPSELL_APP_KEY.ifBlank { if (BuildConfig.DEBUG) TEST_APP_KEY else "" }

    val rewardedZoneId: String
        get() = BuildConfig.TAPSELL_REWARDED_ZONE_ID.ifBlank { if (BuildConfig.DEBUG) TEST_REWARDED_ZONE else "" }

    val isConfigured: Boolean
        get() = appKey.isNotBlank() && rewardedZoneId.isNotBlank()
}
