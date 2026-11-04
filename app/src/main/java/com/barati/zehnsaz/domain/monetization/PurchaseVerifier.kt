package com.barati.zehnsaz.domain.monetization

import javax.inject.Inject
import javax.inject.Singleton

enum class VerificationResult {
    /** The purchase is genuine. */
    VERIFIED,
    /** The verifier is sure the purchase is not genuine (forged, refunded, or used by another account). */
    REJECTED,
    /** The verifier could not be reached. The caller delivers anyway and does not punish an offline player. */
    UNAVAILABLE
}

/**
 * A second check of a store purchase after the on-device signature check that Poolakey does.
 *
 * The server side is ready (POST /api/billing/bazaar/verify, see server/README.md). Once the app
 * signs in to the server, bind an implementation that calls it in MonetizationModule.
 */
interface PurchaseVerifier {
    suspend fun verifyProduct(productId: String, purchaseToken: String): VerificationResult
}

/** Trusts Poolakey's on-device RSA signature check. Used until the app talks to the server. */
@Singleton
class OnDevicePurchaseVerifier @Inject constructor() : PurchaseVerifier {
    override suspend fun verifyProduct(productId: String, purchaseToken: String) = VerificationResult.VERIFIED
}
