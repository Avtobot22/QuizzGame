package com.quizzgame.feature.quizentry.tv.domain.entity

import com.quizzgame.core.error.failure.AppFailure

internal sealed interface QuizEntryResult {
    data class Loaded(val content: QuizEntry) : QuizEntryResult
    data class Failed(val failure: AppFailure) : QuizEntryResult
}
