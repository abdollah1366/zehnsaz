package com.barati.zehnsaz.data.monetization.bazaar

import androidx.activity.ComponentActivity
import com.barati.zehnsaz.domain.model.OwnedPurchase
import com.barati.zehnsaz.domain.model.Product
import com.barati.zehnsaz.domain.model.ProductPurchaseOutcome
import com.barati.zehnsaz.domain.model.ProductType
import com.barati.zehnsaz.domain.model.PurchaseResult
import com.barati.zehnsaz.domain.monetization.ProductCatalog
import com.barati.zehnsaz.domain.monetization.PurchaseProvider
import com.barati.zehnsaz.ui.common.ActivityProvider
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
 * Cafe Bazaar one-time in-app products (coins, gems, boosters, remove ads, packs) through Poolakey,
 * over the shared [BazaarBilling] connection. Delivery and consumption order is decided by
 * ShopManager; this class only talks to the store.
 */
@Singleton
class BazaarPurchaseProvider @Inject constructor(
    private val billing: BazaarBilling,
    private val activityProvider: ActivityProvider
) : PurchaseProvider {

    override val providerName: String = BazaarBilling.PROVIDER

    private val payment get() = billing.payment

    override fun isAvailable(): Boolean = billing.isAvailable()

    override suspend fun getProducts(productIds: List<String>): List<Product> {
        if (productIds.isEmpty() || !billing.ensureConnected()) return emptyList()
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<List<Product>> { cont ->
                payment.getInAppSkuDetails(skuIds = productIds) {
                    getSkuDetailsSucceed { details ->
                        if (cont.isActive) cont.resume(details.map { sku ->
                            Product(
                                productId = sku.sku,
                                title = sku.title,
                                description = sku.description,
                                price = sku.price,
                                currency = "IRR",
                                productType = when (ProductCatalog.find(sku.sku)?.kind) {
                                    ProductCatalog.Kind.CONSUMABLE -> ProductType.CONSUMABLE
                                    else -> ProductType.NON_CONSUMABLE
                                }
                            )
                        })
                    }
                    getSkuDetailsFailed { if (cont.isActive) cont.resume(emptyList()) }
                }
            }
        }
    }

    override suspend fun purchase(productId: String): ProductPurchaseOutcome {
        if (!billing.ensureConnected()) {
            return ProductPurchaseOutcome(PurchaseResult.NOT_AVAILABLE, errorMessage = "bazaar_not_connected")
        }
        val activity = activityProvider.getActivity() as? ComponentActivity
            ?: return ProductPurchaseOutcome(PurchaseResult.FAILED, errorMessage = "no_activity")

        // The payload ties this purchase to this request; Bazaar returns it in PurchaseInfo.
        val payload = UUID.randomUUID().toString()
        val request = PurchaseRequest(productId = productId, payload = payload)

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<ProductPurchaseOutcome> { cont ->
                fun finish(outcome: ProductPurchaseOutcome) {
                    if (cont.isActive) cont.resume(outcome)
                }
                payment.purchaseProduct(
                    registry = activity.activityResultRegistry,
                    request = request
                ) {
                    purchaseFlowBegan { }
                    failedToBeginFlow { t ->
                        finish(ProductPurchaseOutcome(PurchaseResult.FAILED, errorMessage = t.javaClass.simpleName + ": " + t.message))
                    }
                    purchaseSucceed { info ->
                        when {
                            info.payload != payload ->
                                finish(ProductPurchaseOutcome(PurchaseResult.FAILED, errorMessage = "payload_mismatch"))
                            info.productId != productId ->
                                finish(ProductPurchaseOutcome(PurchaseResult.FAILED, errorMessage = "product_mismatch"))
                            else ->
                                finish(ProductPurchaseOutcome(PurchaseResult.SUCCESS, info.toOwned()))
                        }
                    }
                    purchaseCanceled { finish(ProductPurchaseOutcome(PurchaseResult.CANCELLED)) }
                    purchaseFailed { t ->
                        finish(ProductPurchaseOutcome(PurchaseResult.FAILED, errorMessage = t.javaClass.simpleName + ": " + t.message))
                    }
                }
            }
        }
    }

    override suspend fun consume(purchaseToken: String): Boolean {
        if (!billing.ensureConnected()) return false
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Boolean> { cont ->
                payment.consumeProduct(purchaseToken = purchaseToken) {
                    consumeSucceed { if (cont.isActive) cont.resume(true) }
                    consumeFailed { if (cont.isActive) cont.resume(false) }
                }
            }
        }
    }

    override suspend fun queryOwnedPurchases(): Result<List<OwnedPurchase>> {
        if (!billing.ensureConnected()) return Result.failure(IllegalStateException("bazaar_not_connected"))
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Result<List<OwnedPurchase>>> { cont ->
                payment.getPurchasedProducts {
                    querySucceed { purchases ->
                        if (cont.isActive) cont.resume(Result.success(
                            purchases
                                .filter { it.purchaseState == PurchaseState.PURCHASED }
                                .map { it.toOwned() }
                        ))
                    }
                    queryFailed { t -> if (cont.isActive) cont.resume(Result.failure(t)) }
                }
            }
        }
    }

    private fun PurchaseInfo.toOwned() = OwnedPurchase(
        productId = productId,
        purchaseToken = purchaseToken,
        orderId = orderId,
        purchaseTime = purchaseTime,
        provider = providerName
    )
}
