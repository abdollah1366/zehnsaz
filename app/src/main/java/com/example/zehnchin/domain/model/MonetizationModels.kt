package com.example.zehnchin.domain.model

enum class ProductType {
    CONSUMABLE,
    NON_CONSUMABLE,
    PREMIUM,
    LEVEL_PACK,
    BOOSTER_PACK
}

data class Product(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val currency: String,
    val productType: ProductType,
    val isAvailable: Boolean = true
)

enum class PurchaseResult {
    SUCCESS,
    CANCELLED,
    FAILED,
    PENDING,
    ALREADY_OWNED,
    NOT_AVAILABLE
}

enum class AdStatus {
    IDLE,
    LOADING,
    READY,
    SHOWING,
    COMPLETED,
    FAILED,
    NOT_AVAILABLE,
    CANCELLED
}

enum class AdResult {
    COMPLETED,
    FAILED,
    NOT_AVAILABLE,
    CANCELLED
}

enum class AdPlacement {
    LEVEL_COMPLETED,
    LEVEL_FAILED,
    SHOP,
    DAILY_REWARD,
    EXTRA_HINT
}

enum class EntitlementType {
    REMOVE_ADS,
    PREMIUM_TRAINING,
    LEVEL_PACK_01
}

data class PurchaseTransaction(
    val transactionId: String,
    val productId: String,
    val purchaseToken: String?,
    val status: PurchaseResult,
    val timestamp: Long,
    val provider: String
)
