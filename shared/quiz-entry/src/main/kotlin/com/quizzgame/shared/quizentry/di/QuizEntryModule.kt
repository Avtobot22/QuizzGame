package com.quizzgame.shared.quizentry.di

import com.quizzgame.shared.quizentry.data.QuizEntryRepositoryImpl
import com.quizzgame.shared.quizentry.domain.repository.QuizEntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface QuizEntryModule {
    @Binds fun repository(implementation: QuizEntryRepositoryImpl): QuizEntryRepository
}
