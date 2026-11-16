package com.quizzgame.feature.feed.tv.di

import com.quizzgame.feature.feed.tv.data.FeedRepositoryImpl
import com.quizzgame.feature.feed.tv.domain.FeedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TvFeedModule {
    @Binds
    abstract fun repository(implementation: FeedRepositoryImpl): FeedRepository
}
