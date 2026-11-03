package com.quizzgame.mobile.di

import android.content.Context
import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.app.network.AppNetworkSettings
import com.quizzgame.app.network.realAppNetworkFactory
import com.quizzgame.app.session.SessionManagerFactory
import com.quizzgame.core.session.AndroidSession
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
        SessionManagerFactory { AndroidSession.open(context) }

    @Provides
    fun networkFactory(settings: AppNetworkSettings): AppNetworkGraphFactory =
        realAppNetworkFactory(settings)
}
