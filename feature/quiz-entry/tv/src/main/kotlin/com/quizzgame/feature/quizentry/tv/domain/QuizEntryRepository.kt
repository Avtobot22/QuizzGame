package com.quizzgame.feature.quizentry.tv.domain

import com.quizzgame.feature.quizentry.tv.domain.entity.QuizEntryResult

internal interface QuizEntryRepository {
    suspend fun load(entryId: String): QuizEntryResult
}
