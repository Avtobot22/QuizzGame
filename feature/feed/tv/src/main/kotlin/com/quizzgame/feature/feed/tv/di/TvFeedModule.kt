package com.quizzgame.feature.feed.tv.di

import com.quizzgame.feature.feed.tv.data.FeedRemoteDataSourceImpl
import com.quizzgame.shared.feed.data.FeedRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class TvFeedModule {
    @Binds
    abstract fun remote(implementation: FeedRemoteDataSourceImpl): FeedRemoteDataSource
}
