package com.quizzgame.feature.auth.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.auth.tv.presentation.TvSignInStep
import com.quizzgame.feature.auth.tv.presentation.TvSignInViewModel
import com.quizzgame.feature.authprovider.tv.ui.TvDeviceAuthScreen

@Composable
internal fun TvSignInScreen(onCancel: () -> Unit, recovery: Boolean = false) {
    val viewModel: TvSignInViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    TvDeviceAuthScreen { startDeviceAuth ->
        val methods = state.step as? TvSignInStep.Methods
        val visible = if (methods != null && startDeviceAuth == null) {
            state.copy(step = methods.copy(google = false))
        } else {
            state
        }
        TvSignInContent(
            visible,
            onIntent = viewModel::onIntent,
            onGoogle = { startDeviceAuth?.invoke() },
            onCancel = onCancel,
            recovery = recovery,
        )
    }
}
