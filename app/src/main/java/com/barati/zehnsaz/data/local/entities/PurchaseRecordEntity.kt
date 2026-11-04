package com.barati.zehnsaz.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One store purchase the app has seen, keyed by the store's purchase token.
 *
 * It makes delivery idempotent: a token is granted once, even if the app is killed between
 * paying and consuming, or the same purchase comes back from a restore.
 */
@Entity(tableName = "purchase_records")
data class PurchaseRecordEntity(
    @PrimaryKey val purchaseToken: String,
    val productId: String,
    val orderId: String,
    val provider: String,
    val purchaseTime: Long,
    /** The reward or entitlement for this purchase has been given to the player. */
    val granted: Boolean,
    /** A consumable was consumed in the store, so it can be bought again. Always false for one-time products. */
    val consumed: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
