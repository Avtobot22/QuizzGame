package com.quizzgame.feature.account.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.account.mobile.presentation.MobileProfileViewModel

@Composable
internal fun MobileProfileScreen(onSignIn: () -> Unit, onSignOut: () -> Unit) {
    val viewModel: MobileProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    MobileProfileContent(state, viewModel::onIntent, onSignIn, onSignOut)
}
