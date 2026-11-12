package com.quizzgame.feature.splash.tv.di

import com.quizzgame.feature.splash.tv.data.TvSplashRemoteDataSource
import com.quizzgame.shared.splash.data.SplashRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface TvSplashModule {
    @Binds fun remote(implementation: TvSplashRemoteDataSource): SplashRemoteDataSource
}
