package com.barati.zehnsaz.data.monetization.bazaar

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import com.barati.zehnsaz.domain.model.PurchaseResult
import com.barati.zehnsaz.domain.monetization.subscription.ActiveSubscription
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionCatalog
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionPlan
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionProvider
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionPurchaseOutcome
import com.barati.zehnsaz.ui.common.ActivityProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.entity.PurchaseInfo
import ir.cafebazaar.poolakey.entity.PurchaseState
import ir.cafebazaar.poolakey.request.PurchaseRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Cafe Bazaar subscriptions through the official Poolakey library.
 *
 * Poolakey verifies each purchase's signature on the device with the app's RSA public key.
 * For stronger protection (e.g. against a patched APK) the purchase token should also be checked
 * on our server with the Bazaar developer API; that is a follow-up once the server has billing.
 */
@Singleton
class BazaarSubscriptionProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityProvider: ActivityProvider
) : SubscriptionProvider {

    override val providerName: String = PROVIDER

    private val payment: Payment by lazy {
        val config = PaymentConfiguration(
            localSecurityCheck = SecurityCheck.Enable(rsaPublicKey = BazaarConfig.rsaPublicKey)
        )
        Payment(context = context, config = config)
    }

    private var connection: Connection? = null
    private val connectMutex = Mutex()

    override fun isAvailable(): Boolean = BazaarConfig.isConfigured && isBazaarInstalled()

    override suspend fun getPlans(planIds: List<String>): List<SubscriptionPlan> {
        if (planIds.isEmpty() || !ensureConnected()) return emptyList()
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<List<SubscriptionPlan>> { cont ->
                payment.getSubscriptionSkuDetails(skuIds = planIds) {
                    getSkuDetailsSucceed { details ->
                        if (cont.isActive) cont.resume(details.map { sku ->
                            SubscriptionPlan(
                                planId = sku.sku,
                                period = SubscriptionCatalog.periodOf(sku.sku),
                                title = sku.title,
                                description = sku.description,
                                price = sku.price
                            )
                        })
                    }
                    getSkuDetailsFailed { if (cont.isActive) cont.resume(emptyList()) }
                }
            }
        }
    }

    override suspend fun subscribe(planId: String): SubscriptionPurchaseOutcome {
        if (!ensureConnected()) {
            return SubscriptionPurchaseOutcome(PurchaseResult.NOT_AVAILABLE, errorMessage = "bazaar_not_connected")
        }
        val activity = activityProvider.getActivity() as? ComponentActivity
            ?: return SubscriptionPurchaseOutcome(PurchaseResult.FAILED, errorMessage = "no_activity")

        // The payload ties this purchase to this request; Bazaar returns it in PurchaseInfo.
        val payload = UUID.randomUUID().toString()
        val request = PurchaseRequest(productId = planId, payload = payload)

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<SubscriptionPurchaseOutcome> { cont ->
                fun finish(outcome: SubscriptionPurchaseOutcome) {
                    if (cont.isActive) cont.resume(outcome)
                }
                payment.subscribeProduct(
                    registry = activity.activityResultRegistry,
                    request = request
                ) {
                    purchaseFlowBegan { }
                    failedToBeginFlow { t ->
                        finish(SubscriptionPurchaseOutcome(PurchaseResult.FAILED, errorMessage = t.message))
                    }
                    purchaseSucceed { info ->
                        if (info.payload != payload) {
                            finish(SubscriptionPurchaseOutcome(PurchaseResult.FAILED, errorMessage = "payload_mismatch"))
                        } else {
                            finish(SubscriptionPurchaseOutcome(PurchaseResult.SUCCESS, info.toActive()))
                        }
                    }
                    purchaseCanceled { finish(SubscriptionPurchaseOutcome(PurchaseResult.CANCELLED)) }
                    purchaseFailed { t ->
                        finish(SubscriptionPurchaseOutcome(PurchaseResult.FAILED, errorMessage = t.message))
                    }
                }
            }
        }
    }

    override suspend fun queryActiveSubscriptions(): Result<List<ActiveSubscription>> {
        if (!ensureConnected()) return Result.failure(IllegalStateException("bazaar_not_connected"))
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Result<List<ActiveSubscription>>> { cont ->
                payment.getSubscribedProducts {
                    querySucceed { purchases ->
                        if (cont.isActive) cont.resume(Result.success(
                            purchases
                                .filter { it.purchaseState == PurchaseState.PURCHASED }
                                .map { it.toActive() }
                        ))
                    }
                    queryFailed { t -> if (cont.isActive) cont.resume(Result.failure(t)) }
                }
            }
        }
    }

    private suspend fun ensureConnected(): Boolean = connectMutex.withLock {
        if (!isAvailable()) return false
        if (connection?.getState() is ConnectionState.Connected) return true
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Boolean> { cont ->
                connection = payment.connect {
                    connectionSucceed { if (cont.isActive) cont.resume(true) }
                    connectionFailed { if (cont.isActive) cont.resume(false) }
                    disconnected { if (cont.isActive) cont.resume(false) }
                }
            }
        }
    }

    private fun isBazaarInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(BazaarConfig.BAZAAR_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun PurchaseInfo.toActive() = ActiveSubscription(
        planId = productId,
        purchaseToken = purchaseToken,
        orderId = orderId,
        purchaseTime = purchaseTime,
        provider = PROVIDER
    )

    companion object {
        const val PROVIDER = "bazaar"
    }
}
