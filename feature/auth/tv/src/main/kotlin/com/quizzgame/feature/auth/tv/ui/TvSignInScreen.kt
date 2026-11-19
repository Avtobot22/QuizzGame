package com.quizzgame.feature.auth.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.auth.tv.presentation.TvSignInViewModel
import com.quizzgame.feature.authprovider.tv.ui.TvDeviceAuthScreen
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpIntent
import com.quizzgame.shared.auth.presentation.OtpStep

@Composable
internal fun TvSignInScreen(onCancel: () -> Unit, recovery: Boolean = false) {
    val viewModel: TvSignInViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    TvDeviceAuthScreen { startDeviceAuth ->
        val methods = state.step as? OtpStep.Methods
        val visible = if (methods != null && startDeviceAuth == null) {
            state.copy(step = methods.copy(choices = methods.choices.filterNot { it == AuthMethod.GOOGLE }))
        } else {
            state
        }
        TvSignInContent(
            visible,
            onIntent = { intent ->
                if (intent is OtpIntent.Choose && intent.method == AuthMethod.GOOGLE) {
                    startDeviceAuth?.invoke()
                } else {
                    viewModel.onIntent(intent)
                }
            },
            onCancel = onCancel,
            recovery = recovery,
        )
    }
}
