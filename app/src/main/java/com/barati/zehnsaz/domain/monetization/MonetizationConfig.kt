package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.domain.economy.BalanceConfig
import com.barati.zehnsaz.domain.model.ItemType
import com.barati.zehnsaz.domain.model.Reward

object MonetizationConfig {
    const val REWARDED_AD_DAILY_LIMIT = 10
    const val REWARDED_AD_COOLDOWN_SECONDS = 60
    
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
