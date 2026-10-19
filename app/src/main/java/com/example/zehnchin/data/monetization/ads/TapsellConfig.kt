package com.example.zehnchin.data.monetization.ads

import com.example.zehnchin.BuildConfig

object TapsellConfig {
    // These should ideally be in a secure place or provided via build variables
    const val APP_KEY = "dummy_app_key_for_now" // Replace with real key in production
    
    const val PLACEMENT_LEVEL_COMPLETED = "dummy_placement_level_completed"
    const val PLACEMENT_LEVEL_FAILED = "dummy_placement_level_failed"
    const val PLACEMENT_SHOP = "dummy_placement_shop"

    // Official Tapsell Test IDs for mid-2026 (Mocked here, but usually provided in docs)
    const val TEST_APP_KEY = "tapsell_test_app_key"
    const val TEST_PLACEMENT_REWARDED = "tapsell_test_rewarded_placement"

    fun getAppKey(): String = if (BuildConfig.DEBUG) TEST_APP_KEY else APP_KEY
    
    fun getPlacementId(internalId: String): String {
        if (BuildConfig.DEBUG) return TEST_PLACEMENT_REWARDED
        return when (internalId) {
            "LEVEL_COMPLETED" -> PLACEMENT_LEVEL_COMPLETED
            "LEVEL_FAILED" -> PLACEMENT_LEVEL_FAILED
            "SHOP" -> PLACEMENT_SHOP
            else -> TEST_PLACEMENT_REWARDED
        }
    }
}
