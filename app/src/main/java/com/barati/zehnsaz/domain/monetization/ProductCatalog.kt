package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.domain.model.EntitlementType
import com.barati.zehnsaz.domain.model.ItemType
import com.barati.zehnsaz.domain.model.Reward

/**
 * The in-app products the shop sells and what each one delivers.
 *
 * Every id here must exist with the same id in the Cafe Bazaar developer panel
 * (your app > In-app products > Products). Prices are set in the panel, not here.
 */
object ProductCatalog {

    enum class Kind {
        /** Bought again and again (coins, gems, boosters). Consumed in Bazaar right after delivery. */
        CONSUMABLE,
        /** Bought once and kept for good (remove ads, packs). Never consumed, so a restore brings it back. */
        ONE_TIME
    }

    data class Entry(
        val productId: String,
        val kind: Kind,
        val reward: Reward = Reward(),
        val entitlement: EntitlementType? = null
    )

    val entries: List<Entry> = listOf(
        Entry("coins_100", Kind.CONSUMABLE, Reward(coins = 100, xp = 10)),
        Entry("coins_500", Kind.CONSUMABLE, Reward(coins = 500, xp = 50)),
        Entry("coins_1200", Kind.CONSUMABLE, Reward(coins = 1200, xp = 150)),
        Entry("gems_10", Kind.CONSUMABLE, Reward(gems = 10, xp = 20)),
        Entry("gems_50", Kind.CONSUMABLE, Reward(gems = 50, xp = 100)),
        Entry("gems_150", Kind.CONSUMABLE, Reward(gems = 150, xp = 400)),
        Entry("booster_pack_small", Kind.CONSUMABLE, Reward(items = mapOf(ItemType.HINT to 2, ItemType.UNDO to 2))),
        Entry(
            "booster_pack_large", Kind.CONSUMABLE,
            Reward(items = mapOf(ItemType.HINT to 10, ItemType.UNDO to 10, ItemType.SHUFFLE to 5))
        ),
        Entry("remove_ads", Kind.ONE_TIME, Reward(xp = 100), EntitlementType.REMOVE_ADS)
        // "premium_training_pack" and "level_pack_01" (EntitlementType.PREMIUM_TRAINING / LEVEL_PACK_01)
        // are left out until there is content behind them, so the shop never sells something that unlocks nothing.
    )

    private val byId = entries.associateBy { it.productId }

    val productIds: List<String> get() = entries.map { it.productId }

    fun find(productId: String): Entry? = byId[productId]
}
