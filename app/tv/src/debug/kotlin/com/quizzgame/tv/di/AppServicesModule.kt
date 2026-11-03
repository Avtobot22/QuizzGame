package com.quizzgame.tv.di

import android.content.Context
import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.app.network.AppNetworkSettings
import com.quizzgame.app.session.SessionManagerFactory
import com.quizzgame.debug.network.debugAppNetworkFactory
import com.quizzgame.debug.startup.openDebugSession
import com.quizzgame.tv.BuildConfig
import com.quizzgame.tv.network.mocks.tvMockRules
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
    fun networkFactory(@ApplicationContext context: Context, settings: AppNetworkSettings): AppNetworkGraphFactory =
        debugAppNetworkFactory(
            context,
            settings,
            "mock/bodies/refresh.json",
            BuildConfig.DEBUG_FAIL_NETWORK_ONCE,
            ::tvMockRules,
        )
}
