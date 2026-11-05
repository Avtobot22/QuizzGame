package com.quizzgame.mobile.di

import android.content.Context
import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.app.network.AppNetworkSettings
import com.quizzgame.app.session.SessionManagerFactory
import com.quizzgame.core.session.refresh.RefreshRevocationPolicy
import com.quizzgame.debug.network.debugAppNetworkFactory
import com.quizzgame.debug.startup.openDebugSession
import com.quizzgame.mobile.BuildConfig
import com.quizzgame.mobile.network.mocks.mobileMockRules
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppServicesModule {
    @Provides
    fun sessionManagerFactory(@ApplicationContext context: Context): SessionManagerFactory =
        SessionManagerFactory { openDebugSession(context, BuildConfig.DEBUG_SESSION_SCENARIO) }

    @Provides
    fun networkFactory(
        @ApplicationContext context: Context,
        settings: AppNetworkSettings,
        revocationPolicy: RefreshRevocationPolicy,
    ): AppNetworkGraphFactory =
        debugAppNetworkFactory(
            context,
            settings,
            "mock/bodies/mobile-refresh.json",
            BuildConfig.DEBUG_FAIL_NETWORK_ONCE,
            revocationPolicy,
            ::mobileMockRules,
        )
}
