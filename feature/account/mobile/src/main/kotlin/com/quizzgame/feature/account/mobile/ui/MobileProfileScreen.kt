package com.quizzgame.feature.account.mobile.ui

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
internal fun MobileProfileScreen(onSignIn: () -> Unit, onSignOut: () -> Unit) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle, viewModel) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { viewModel.onIntent(ProfileIntent.Visible) }
    }
    MobileProfileContent(state, viewModel::onIntent, onSignIn, onSignOut)
}
