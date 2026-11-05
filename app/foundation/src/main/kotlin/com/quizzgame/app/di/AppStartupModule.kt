package com.quizzgame.app.di

import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.app.network.StartupApiCallExecutor
import com.quizzgame.app.session.SessionManagerFactory
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupCoordinator
import com.quizzgame.core.network.api.ApiCallExecutor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppStartupModule {
    @Provides
    @Singleton
    fun apiCallExecutor(startup: AppStartup): ApiCallExecutor = StartupApiCallExecutor(startup)

    @Provides
    @Singleton
    fun startup(sessionFactory: SessionManagerFactory, networkFactory: AppNetworkGraphFactory): AppStartup =
        AppStartupCoordinator(sessionFactory::open, networkFactory, Dispatchers.IO)
}
