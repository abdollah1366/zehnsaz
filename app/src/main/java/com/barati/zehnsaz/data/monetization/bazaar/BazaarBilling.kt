package com.barati.zehnsaz.data.monetization.bazaar

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * The one Poolakey [Payment] and connection to the Bazaar app, shared by in-app products and
 * subscriptions so the app never opens two billing connections.
 *
 * Poolakey verifies each purchase's signature on the device with the app's RSA public key.
 */
@Singleton
class BazaarBilling @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val payment: Payment by lazy {
        val config = PaymentConfiguration(
            localSecurityCheck = SecurityCheck.Enable(rsaPublicKey = BazaarConfig.rsaPublicKey)
        )
        Payment(context = context, config = config)
    }

    private var connection: Connection? = null
    private val connectMutex = Mutex()

    fun isAvailable(): Boolean = BazaarConfig.isConfigured && isBazaarInstalled()

    /**
     * Connects to the Bazaar app if needed. Returns false when Bazaar is missing, not configured,
     * or does not answer within [CONNECT_TIMEOUT_MS] (a stuck connection must not freeze the shop).
     */
    suspend fun ensureConnected(): Boolean = connectMutex.withLock {
        if (!isAvailable()) return@withLock false
        if (connection?.getState() is ConnectionState.Connected) return@withLock true
        withContext(Dispatchers.Main) {
            withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
                suspendCancellableCoroutine<Boolean> { cont ->
                    connection = payment.connect {
                        connectionSucceed { if (cont.isActive) cont.resume(true) }
                        connectionFailed { if (cont.isActive) cont.resume(false) }
                        disconnected { if (cont.isActive) cont.resume(false) }
                    }
                }
            } ?: false
        }
    }

    private fun isBazaarInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(BazaarConfig.BAZAAR_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    companion object {
        const val PROVIDER = "bazaar"
        private const val CONNECT_TIMEOUT_MS = 10_000L
    }
}
