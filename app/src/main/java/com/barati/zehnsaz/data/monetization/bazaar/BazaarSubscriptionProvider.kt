package com.barati.zehnsaz.data.monetization.bazaar

import androidx.activity.ComponentActivity
import com.barati.zehnsaz.domain.model.PurchaseResult
import com.barati.zehnsaz.domain.monetization.subscription.ActiveSubscription
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionCatalog
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionPlan
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionProvider
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionPurchaseOutcome
import com.barati.zehnsaz.ui.common.ActivityProvider
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.entity.PurchaseInfo
import ir.cafebazaar.poolakey.entity.PurchaseState
import ir.cafebazaar.poolakey.request.PurchaseRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Cafe Bazaar subscriptions through the official Poolakey library, over the shared [BazaarBilling]
 * connection. For a server-side check of a subscription token, see POST /api/billing/bazaar/verify.
 */
@Singleton
class BazaarSubscriptionProvider @Inject constructor(
    private val billing: BazaarBilling,
    private val activityProvider: ActivityProvider
) : SubscriptionProvider {

    override val providerName: String = PROVIDER

    private val payment: Payment get() = billing.payment

    override fun isAvailable(): Boolean = billing.isAvailable()

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

    private suspend fun ensureConnected(): Boolean = billing.ensureConnected()

    private fun PurchaseInfo.toActive() = ActiveSubscription(
        planId = productId,
        purchaseToken = purchaseToken,
        orderId = orderId,
        purchaseTime = purchaseTime,
        provider = PROVIDER
    )

    companion object {
        const val PROVIDER = BazaarBilling.PROVIDER
    }
}
