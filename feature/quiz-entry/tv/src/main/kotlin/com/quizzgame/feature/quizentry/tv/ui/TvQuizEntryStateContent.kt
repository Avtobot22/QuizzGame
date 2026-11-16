package com.quizzgame.feature.quizentry.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryIntent
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryState

@Composable
internal fun TvQuizEntryStateContent(
    state: TvQuizEntryState,
    onIntent: (TvQuizEntryIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
    onSignIn: () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        when (state) {
            TvQuizEntryState.Loading -> TvQuizEntryLoading()
            is TvQuizEntryState.Error -> TvQuizEntryError(
                state.spec,
                { onIntent(TvQuizEntryIntent.Retry) },
                onSignIn,
            )
            is TvQuizEntryState.Content -> TvQuizEntryContent(state.value, onBack, Modifier.fillMaxSize())
        }
    }
}
