package com.quizzgame.feature.account.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.account.tv.presentation.TvProfileIntent
import com.quizzgame.feature.account.tv.presentation.TvProfileViewModel

@Composable
fun TvProfileScreen(onSignIn: () -> Unit, onLogout: () -> Unit, onSettings: () -> Unit) {
    val model: TvProfileViewModel = hiltViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    TvProfileContent(state, onSignIn, onLogout, onSettings, { model.onIntent(TvProfileIntent.RETRY) })
}
