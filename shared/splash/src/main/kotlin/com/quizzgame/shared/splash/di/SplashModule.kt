package com.quizzgame.shared.splash.di

import com.quizzgame.shared.splash.data.CurrentUserStore
import com.quizzgame.shared.splash.data.SplashRepositoryImpl
import com.quizzgame.shared.splash.domain.repository.CurrentUserReader
import com.quizzgame.shared.splash.domain.repository.SplashRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface SplashModule {
    @Binds fun currentUser(implementation: CurrentUserStore): CurrentUserReader

    @Binds fun repository(implementation: SplashRepositoryImpl): SplashRepository
}
