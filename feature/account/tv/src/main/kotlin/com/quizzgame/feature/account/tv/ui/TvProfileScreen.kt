package com.quizzgame.feature.account.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.quizzgame.shared.account.presentation.ProfileIntent
import com.quizzgame.shared.account.presentation.ProfileViewModel

@Composable
fun TvProfileScreen(onSignIn: () -> Unit, onLogout: () -> Unit, onSettings: () -> Unit) {
    val model: ProfileViewModel = hiltViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle, model) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { model.onIntent(ProfileIntent.Visible) }
    }
    TvProfileContent(state, onSignIn, onLogout, onSettings, model::onIntent)
}
