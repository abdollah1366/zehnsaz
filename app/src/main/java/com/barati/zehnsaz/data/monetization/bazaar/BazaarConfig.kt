package com.barati.zehnsaz.data.monetization.bazaar

import com.barati.zehnsaz.BuildConfig

/**
 * Cafe Bazaar in-app billing settings. Values come from local.properties (or gradle.properties)
 * through BuildConfig; see app/build.gradle.kts and the PR description for where to find them.
 */
object BazaarConfig {
    /** "RSA public key" from the Bazaar developer panel: your app > In-app billing (پرداخت درون‌برنامه‌ای). */
    val rsaPublicKey: String get() = BuildConfig.BAZAAR_RSA_PUBLIC_KEY

    const val BAZAAR_PACKAGE = "com.farsitel.bazaar"

    val isConfigured: Boolean get() = rsaPublicKey.isNotBlank()
}
