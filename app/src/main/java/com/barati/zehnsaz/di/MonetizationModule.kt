package com.barati.zehnsaz.di

import com.barati.zehnsaz.BuildConfig
import com.barati.zehnsaz.data.monetization.*
import com.barati.zehnsaz.data.monetization.ads.*
import com.barati.zehnsaz.data.monetization.bazaar.BazaarConfig
import com.barati.zehnsaz.data.monetization.bazaar.BazaarSubscriptionProvider
import com.barati.zehnsaz.domain.monetization.*
import com.barati.zehnsaz.domain.monetization.subscription.SubscriptionProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonetizationModule {

    @Provides
    @Singleton
    fun providePurchaseProvider(fake: FakePurchaseProvider): PurchaseProvider {
        // Keep fake for now as we only do ads in this phase
        return fake 
    }

    @Provides
    @Singleton
    fun provideRewardedAdProvider(
        fake: FakeRewardedAdProvider,
        production: ProductionRewardedAdProvider
    ): RewardedAdProvider {
        // Real Tapsell whenever a key/zone is set (debug builds use Tapsell's test key and zone).
        // Release builds without a key get the production provider, which reports "no ad".
        return if (TapsellConfig.isConfigured || !BuildConfig.DEBUG) production else fake
    }

    @Provides
    @Singleton
    fun provideSubscriptionProvider(
        fake: FakeSubscriptionProvider,
        bazaar: BazaarSubscriptionProvider
    ): SubscriptionProvider {
        // Debug builds without a Bazaar key use a fake store so the flow can be tried anywhere.
        return if (BazaarConfig.isConfigured || !BuildConfig.DEBUG) bazaar else fake
    }

    @Provides
    @Singleton
    fun provideInterstitialAdProvider(fake: FakeInterstitialAdProvider): InterstitialAdProvider {
        return fake
    }

    @Provides
    @Singleton
    fun provideAnalyticsProvider(impl: FakeAnalyticsProvider): AnalyticsProvider {
        return impl
    }
}
