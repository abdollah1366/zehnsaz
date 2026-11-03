package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.domain.economy.BalanceConfig
import com.barati.zehnsaz.domain.model.ItemType
import com.barati.zehnsaz.domain.model.Reward

object MonetizationConfig {
    // Ad policy (see AdManager): rewarded, opt-in only, capped so that training stays the focus.
    const val REWARDED_AD_DAILY_LIMIT = 8
    const val REWARDED_AD_COOLDOWN_SECONDS = 90

    /** A level must be failed this many times in a row before "skip with an ad" is offered. */
    const val FAILURES_BEFORE_AD_SKIP = 3
    
    val REWARDED_COIN_REWARD = Reward(coins = BalanceConfig.REWARDED_AD_COINS)
    val REWARDED_GEM_REWARD = Reward(gems = BalanceConfig.REWARDED_AD_GEMS)
    
    val LEVEL_COMPLETION_BONUS_MULTIPLIER = 1.5f

    val PRODUCT_IDS = listOf(
        "coins_100", "coins_500", "coins_1200",
        "gems_10", "gems_50", "gems_150",
        "booster_pack_small", "booster_pack_large",
        "premium_training_pack", "remove_ads",
        "level_pack_01"
    )
}
