package com.quizzgame.app.di

import com.quizzgame.core.coroutines.DefaultDispatcherProvider
import com.quizzgame.core.coroutines.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
    @Provides
    @Singleton
    fun dispatchers(): DispatcherProvider = DefaultDispatcherProvider()
}
