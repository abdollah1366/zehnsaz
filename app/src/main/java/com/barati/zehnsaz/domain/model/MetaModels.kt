package com.barati.zehnsaz.domain.model

enum class CurrencyType {
    COIN,
    GEM
}

enum class TransactionType {
    EARN,
    SPEND
}

enum class RewardSource {
    LEVEL_REWARD,
    DAILY_REWARD,
    ACHIEVEMENT,
    STREAK,
    SHOP,
    BONUS,
    OTHER
}

enum class ItemType {
    UNDO,
    HINT,
    SHUFFLE,
    TIME_BOOST,
    LIFE
}

data class Reward(
    val xp: Int = 0,
    val coins: Int = 0,
    val gems: Int = 0,
    val items: Map<ItemType, Int> = emptyMap()
)

enum class GameEvent {
    LEVEL_COMPLETED,
    CHAPTER_COMPLETED,
    STAR_EARNED,
    PLAYER_LEVEL_UP,
    DAILY_LOGIN,
    DAILY_REWARD_CLAIMED,
    STREAK_UPDATED,
    ACHIEVEMENT_UNLOCKED,
    PERSONAL_BEST
}