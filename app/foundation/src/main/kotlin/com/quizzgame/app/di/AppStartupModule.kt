package com.quizzgame.app.di

import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.app.network.StartupApiCallExecutor
import com.quizzgame.app.session.SessionManagerFactory
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupCoordinator
import com.quizzgame.app.startup.GraphAccessResult
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.session.lifecycle.SessionStateReader
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
    fun sessionStateReader(startup: AppStartup): SessionStateReader =
        SessionStateReader {
            when (val access = startup.accessGraph()) {
                is GraphAccessResult.Ready -> access.graph.session.state.value
                else -> startup.sessionState.value
            }
        }

    @Provides
    @Singleton
    fun apiCallExecutor(startup: AppStartup): ApiCallExecutor = StartupApiCallExecutor(startup)

    @Provides
    @Singleton
    fun startup(sessionFactory: SessionManagerFactory, networkFactory: AppNetworkGraphFactory): AppStartup =
        AppStartupCoordinator(sessionFactory::open, networkFactory, Dispatchers.IO)
}
