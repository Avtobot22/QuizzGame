package com.quizzgame.feature.quizentry.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.feature.quizentry.mobile.presentation.MobileQuizEntryIntent
import com.quizzgame.feature.quizentry.mobile.presentation.MobileQuizEntryState
import com.quizzgame.feature.quizentry.mobile.presentation.MobileQuizEntryViewModel

@Composable
internal fun MobileQuizEntryScreen(entryId: String, onBack: () -> Unit, onPlay: () -> Boolean, onSignIn: () -> Unit) {
    val viewModel: MobileQuizEntryViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(entryId) { viewModel.onIntent(MobileQuizEntryIntent.Load(entryId)) }
    NavigationResultEffect(
        (state as? MobileQuizEntryState.Content)?.destination,
        { viewModel.onIntent(MobileQuizEntryIntent.NavigationHandled) },
        { onPlay() },
    )
    MobileQuizEntryStateContent(state, viewModel::onIntent, onBack, onSignIn)
}
