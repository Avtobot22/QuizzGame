package com.quizzgame.feature.account.tv.di

import com.quizzgame.feature.account.tv.data.TvAccountRemoteDataSource
import com.quizzgame.shared.account.data.AccountRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface TvAccountModule {
    @Binds
    fun remote(implementation: TvAccountRemoteDataSource): AccountRemoteDataSource
}
