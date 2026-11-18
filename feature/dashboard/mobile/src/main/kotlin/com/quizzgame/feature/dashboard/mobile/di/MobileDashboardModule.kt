package com.quizzgame.feature.dashboard.mobile.di

import com.quizzgame.feature.dashboard.mobile.data.AccountRepositoryImpl
import com.quizzgame.feature.dashboard.mobile.data.DashboardRepositoryImpl
import com.quizzgame.feature.dashboard.mobile.domain.AccountRepository
import com.quizzgame.feature.dashboard.mobile.domain.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MobileDashboardModule {
    @Binds
    abstract fun account(implementation: AccountRepositoryImpl): AccountRepository

    @Binds
    abstract fun repository(implementation: DashboardRepositoryImpl): DashboardRepository
}
