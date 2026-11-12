package com.quizzgame.feature.splash.mobile.di

import com.quizzgame.feature.splash.mobile.data.MobileSplashRemoteDataSource
import com.quizzgame.shared.splash.data.SplashRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface MobileSplashModule {
    @Binds fun remote(implementation: MobileSplashRemoteDataSource): SplashRemoteDataSource
}
