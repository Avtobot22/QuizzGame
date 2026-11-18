package com.quizzgame.feature.quizentry.mobile.presentation

import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.shared.catalog.domain.entity.QuizEntry

internal sealed interface MobileQuizEntryState {
    data object Loading : MobileQuizEntryState
    data class Content(
        val value: QuizEntry,
        val destination: MobileQuizEntryDestination? = null,
    ) : MobileQuizEntryState
    data class Error(val spec: ErrorPresentationSpec) : MobileQuizEntryState
}
