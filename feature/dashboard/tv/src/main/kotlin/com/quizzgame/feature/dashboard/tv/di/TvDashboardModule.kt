package com.quizzgame.feature.dashboard.tv.di

import com.quizzgame.feature.dashboard.tv.data.DashboardRepositoryImpl
import com.quizzgame.feature.dashboard.tv.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TvDashboardModule {
    @Binds
    abstract fun repository(implementation: DashboardRepositoryImpl): DashboardRepository
}
