package com.quizzgame.shared.quizentry.domain.entity

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.catalog.domain.entity.QuizEntry

sealed interface QuizEntryResult {
    data class Loaded(val content: QuizEntry) : QuizEntryResult
    data class Failed(val failure: AppFailure) : QuizEntryResult
}
