package com.quizzgame.feature.quizentry.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryIntent
import com.quizzgame.feature.quizentry.tv.presentation.TvQuizEntryViewModel

@Composable
fun TvQuizEntryScreen(entryId: String, onBack: () -> Unit, modifier: Modifier = Modifier, onSignIn: () -> Unit = {}) {
    val viewModel: TvQuizEntryViewModel = hiltViewModel(key = entryId)
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(entryId) { viewModel.onIntent(TvQuizEntryIntent.Load(entryId)) }
    TvQuizEntryStateContent(state, viewModel::onIntent, onBack, modifier, onSignIn)
}
