package com.quizzgame.feature.quizentry.tv.di

import com.quizzgame.feature.quizentry.tv.data.TvQuizEntryRemoteDataSource
import com.quizzgame.shared.quizentry.data.QuizEntryRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface TvQuizEntryModule {
    @Binds fun remote(implementation: TvQuizEntryRemoteDataSource): QuizEntryRemoteDataSource
}
