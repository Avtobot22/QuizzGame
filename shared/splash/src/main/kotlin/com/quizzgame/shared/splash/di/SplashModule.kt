package com.quizzgame.shared.splash.di

import com.quizzgame.shared.splash.data.SplashRepositoryImpl
import com.quizzgame.shared.splash.domain.SplashRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface SplashModule {
    @Binds fun repository(implementation: SplashRepositoryImpl): SplashRepository
}
