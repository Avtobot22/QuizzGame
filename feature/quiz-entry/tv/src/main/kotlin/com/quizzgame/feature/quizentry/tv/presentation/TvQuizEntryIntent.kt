package com.quizzgame.feature.quizentry.tv.presentation

internal sealed interface TvQuizEntryIntent {
    data class Load(val entryId: String) : TvQuizEntryIntent
    data object Retry : TvQuizEntryIntent
}
