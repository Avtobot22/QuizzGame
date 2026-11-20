package com.quizzgame.shared.account.di

import com.quizzgame.shared.account.data.PlayerProfileRepositoryImpl
import com.quizzgame.shared.account.domain.repository.PlayerProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface AccountModule {
    @Binds
    fun nickname(implementation: PlayerProfileRepositoryImpl): PlayerProfileRepository
}
