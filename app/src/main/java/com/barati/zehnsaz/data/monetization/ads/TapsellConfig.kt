package com.barati.zehnsaz.data.monetization.ads

import com.barati.zehnsaz.BuildConfig

/**
 * Tapsell Mediation settings. The app key goes into the manifest (Tapsell reads it on start-up);
 * the zone id is used when requesting each rewarded ad. Both come from local.properties or
 * gradle.properties through app/build.gradle.kts. Debug builds fall back to Tapsell's public
 * test key and test rewarded zone, so ads can be tried before the real ones exist.
 */
object TapsellConfig {
    /** Tapsell's official sample key/zone (TapsellMediation-AndroidSample). Test ads only. */
    const val TEST_APP_KEY = "76798342-99a7-4a5f-bf5a-60a088d5dcfb"
    const val TEST_REWARDED_ZONE = "1d710cc7-5e96-46ac-a3e9-8463300333e6"

    val rewardedZoneId: String
        get() = BuildConfig.TAPSELL_REWARDED_ZONE_ID.ifBlank {
            if (BuildConfig.DEBUG) TEST_REWARDED_ZONE else ""
        }

    /** True when a real (or, in debug, test) app key and zone are present. */
    val isConfigured: Boolean
        get() = BuildConfig.TAPSELL_APP_KEY_CONFIGURED && rewardedZoneId.isNotBlank()
}
