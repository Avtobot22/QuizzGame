package com.quizzgame.tv.di

import com.quizzgame.app.network.AppNetworkSettings
import com.quizzgame.core.network.config.NetworkTimeouts
import com.quizzgame.core.network.config.PlatformHeaders
import com.quizzgame.core.session.refresh.ContractRefreshRevocationPolicy
import com.quizzgame.core.session.refresh.RefreshRevocationPolicy
import com.quizzgame.core.session.refresh.api.RefreshEndpoint
import com.quizzgame.tv.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Module
@InstallIn(SingletonComponent::class)
object NetworkSettingsModule {
    @Provides
    @Singleton
    fun refreshRevocationPolicy(): RefreshRevocationPolicy = ContractRefreshRevocationPolicy()

    @Provides
    @Singleton
    fun settings(): AppNetworkSettings = AppNetworkSettings(
        apiBaseUrl = BuildConfig.API_BASE_URL,
        headers = PlatformHeaders(BuildConfig.CLIENT_PLATFORM, BuildConfig.USER_AGENT),
        refreshEndpoint = RefreshEndpoint("api/tv/auth/refresh"),
        timeouts = NetworkTimeouts(
            BuildConfig.CONNECT_TIMEOUT_MS.milliseconds,
            BuildConfig.READ_TIMEOUT_MS.milliseconds,
            BuildConfig.WRITE_TIMEOUT_MS.milliseconds,
            BuildConfig.CALL_TIMEOUT_MS.milliseconds,
        ),
        networkMode = BuildConfig.NETWORK_MODE,
    )
}
