package com.quizzgame.shared.feed.di

import com.quizzgame.shared.feed.data.FeedRepositoryImpl
import com.quizzgame.shared.feed.domain.FeedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal interface FeedModule {
    @Binds
    @ViewModelScoped
    fun repository(implementation: FeedRepositoryImpl): FeedRepository
}
