package com.quizzgame.feature.quizentry.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryIntent
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryState
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryViewModel
import com.quizzgame.feature.quizentry.tv.ui.content.TvQuizEntryStateContent

@Composable
fun TvQuizEntryScreen(
    entryId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onSignIn: () -> Unit = {},
    authenticated: Boolean = false,
    onLobby: (String) -> Boolean = { false },
) {
    val viewModel: TvQuizEntryViewModel = hiltViewModel(key = entryId)
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(entryId) { viewModel.onIntent(TvQuizEntryIntent.Load(entryId)) }
    val destination = (state as? TvQuizEntryState.Content)?.room?.destination
    LaunchedEffect(destination) {
        if (destination != null && onLobby(destination.toString())) viewModel.onIntent(TvQuizEntryIntent.RoomOpened)
    }
    TvQuizEntryStateContent(state, viewModel::onIntent, onBack, modifier, onSignIn, authenticated)
}
