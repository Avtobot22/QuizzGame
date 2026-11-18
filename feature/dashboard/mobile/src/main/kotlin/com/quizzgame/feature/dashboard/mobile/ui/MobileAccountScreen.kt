package com.quizzgame.feature.dashboard.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.dashboard.mobile.presentation.MobileAccountViewModel

@Composable
internal fun MobileAccountScreen(library: Boolean, onSignIn: () -> Unit, onLogout: () -> Unit) {
    val viewModel: MobileAccountViewModel = hiltViewModel()
    val account by viewModel.state.collectAsStateWithLifecycle()
    MobileAccountContent(account, library, viewModel::onIntent, onSignIn, onLogout)
}
