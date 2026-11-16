package com.quizzgame.app.di

import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.app.network.StartupApiCallExecutor
import com.quizzgame.app.session.SessionManagerFactory
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupCoordinator
import com.quizzgame.app.startup.GraphAccessResult
import com.quizzgame.core.coroutines.DispatcherProvider
import com.quizzgame.core.network.api.ApiCallExecutor
import com.quizzgame.core.network.config.createNetworkJson
import com.quizzgame.core.session.lifecycle.SessionStateReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppStartupModule {
    @Provides
    @Singleton
    fun json(): Json = createNetworkJson()

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
    fun apiCallExecutor(startup: AppStartup, dispatchers: DispatcherProvider): ApiCallExecutor =
        StartupApiCallExecutor(startup, dispatchers)

    @Provides
    @Singleton
    fun startup(
        sessionFactory: SessionManagerFactory,
        networkFactory: AppNetworkGraphFactory,
        json: Json,
        dispatchers: DispatcherProvider,
    ): AppStartup =
        AppStartupCoordinator(sessionFactory::open, networkFactory, dispatchers.io, json)
}
