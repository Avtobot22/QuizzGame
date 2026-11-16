package com.quizzgame.feature.quizentry.tv.di

import com.quizzgame.feature.quizentry.tv.data.QuizEntryRepositoryImpl
import com.quizzgame.feature.quizentry.tv.domain.QuizEntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TvQuizEntryModule {
    @Binds
    abstract fun repository(implementation: QuizEntryRepositoryImpl): QuizEntryRepository
}
