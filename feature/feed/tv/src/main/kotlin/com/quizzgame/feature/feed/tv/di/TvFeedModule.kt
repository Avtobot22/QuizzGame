package com.quizzgame.feature.feed.tv.di

import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSource
import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSourceImpl
import com.quizzgame.feature.feed.tv.data.FeedRepositoryImpl
import com.quizzgame.feature.feed.tv.domain.FeedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class TvFeedModule {
    @Binds
    @ViewModelScoped
    abstract fun repository(implementation: FeedRepositoryImpl): FeedRepository

    @Binds
    abstract fun remote(implementation: FeedRemoteDataSourceImpl): FeedRemoteDataSource
}
