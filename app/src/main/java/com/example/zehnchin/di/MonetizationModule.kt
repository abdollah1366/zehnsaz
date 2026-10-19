package com.example.zehnchin.di

import com.example.zehnchin.BuildConfig
import com.example.zehnchin.data.monetization.*
import com.example.zehnchin.data.monetization.ads.*
import com.example.zehnchin.domain.monetization.*
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
        return if (BuildConfig.DEBUG) fake else production
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
