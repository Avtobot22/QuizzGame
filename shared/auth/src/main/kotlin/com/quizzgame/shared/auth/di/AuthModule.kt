package com.quizzgame.shared.auth.di

import com.quizzgame.shared.auth.data.AuthRepositoryImpl
import com.quizzgame.shared.auth.domain.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface AuthModule {
    @Binds
    fun repository(implementation: AuthRepositoryImpl): AuthRepository
}
