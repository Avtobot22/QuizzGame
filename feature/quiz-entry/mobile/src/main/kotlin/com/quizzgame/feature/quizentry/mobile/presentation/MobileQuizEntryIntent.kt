package com.quizzgame.feature.quizentry.mobile.presentation

internal sealed interface MobileQuizEntryIntent {
    data object Connect : MobileQuizEntryIntent
    data object NavigationHandled : MobileQuizEntryIntent
    data class Load(val entryId: String) : MobileQuizEntryIntent
    data object Retry : MobileQuizEntryIntent
}
