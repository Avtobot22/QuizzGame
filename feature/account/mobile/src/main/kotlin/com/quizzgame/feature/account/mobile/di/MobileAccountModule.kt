package com.quizzgame.feature.account.mobile.di

import com.quizzgame.feature.account.mobile.data.MobileAccountRemoteDataSource
import com.quizzgame.shared.account.data.AccountRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface MobileAccountModule {
    @Binds
    fun remote(implementation: MobileAccountRemoteDataSource): AccountRemoteDataSource
}
