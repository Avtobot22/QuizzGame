package com.quizzgame.feature.quizentry.tv.presentation

import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.shared.catalog.domain.entity.QuizEntry

internal sealed interface TvQuizEntryState {
    data object Loading : TvQuizEntryState
    data class Content(val value: QuizEntry) : TvQuizEntryState
    data class Error(val spec: ErrorPresentationSpec) : TvQuizEntryState
}
