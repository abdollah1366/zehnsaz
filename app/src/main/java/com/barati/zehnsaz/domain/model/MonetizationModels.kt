package com.barati.zehnsaz.domain.model

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
    NOT_AVAILABLE,
    /** The store took the payment but our verification rejected the purchase token. */
    VERIFICATION_FAILED
}

/** A purchase the store has confirmed (signature checked on the device by Poolakey). */
data class OwnedPurchase(
    val productId: String,
    val purchaseToken: String,
    val orderId: String,
    val purchaseTime: Long,
    val provider: String
)

data class ProductPurchaseOutcome(
    val result: PurchaseResult,
    val purchase: OwnedPurchase? = null,
    /** A short machine-readable reason for logs and analytics, never shown to the user. */
    val errorMessage: String? = null
)

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
    LEVEL_PACK_01,
    PREMIUM_SUBSCRIPTION
}

data class PurchaseTransaction(
    val transactionId: String,
    val productId: String,
    val purchaseToken: String?,
    val status: PurchaseResult,
    val timestamp: Long,
    val provider: String
)
