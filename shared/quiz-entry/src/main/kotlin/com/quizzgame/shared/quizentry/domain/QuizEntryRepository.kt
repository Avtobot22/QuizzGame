package com.quizzgame.shared.quizentry.domain

import com.quizzgame.shared.quizentry.domain.entity.QuizEntryResult

interface QuizEntryRepository {
    suspend fun load(entryId: String): QuizEntryResult
}
