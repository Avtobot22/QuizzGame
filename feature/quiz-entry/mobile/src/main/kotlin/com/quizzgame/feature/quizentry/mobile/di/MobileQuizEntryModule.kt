package com.quizzgame.feature.quizentry.mobile.di

import com.quizzgame.feature.quizentry.mobile.data.MobileQuizEntryRemoteDataSource
import com.quizzgame.shared.quizentry.data.QuizEntryRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface MobileQuizEntryModule {
    @Binds fun remote(implementation: MobileQuizEntryRemoteDataSource): QuizEntryRemoteDataSource
}
