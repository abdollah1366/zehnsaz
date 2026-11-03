package com.barati.zehnsaz

import android.app.Application
import com.barati.zehnsaz.data.monetization.ads.TapsellInitializer
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionManager
import com.barati.zehnsaz.reminder.DailyReminder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ZehnchinApp : Application() {

    @Inject
    lateinit var subscriptionManager: SubscriptionManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        DailyReminder.createChannel(this)
        TapsellInitializer.initialize(this)
        // Offline-first: restore the last known subscription, then confirm it with the store.
        appScope.launch {
            subscriptionManager.loadCachedStatus()
            subscriptionManager.refresh()
        }
    }
}