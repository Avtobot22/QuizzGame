package com.quizzgame.feature.auth.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInViewModel
import com.quizzgame.feature.authprovider.mobile.ui.MobileOAuthStatus
import com.quizzgame.feature.authprovider.mobile.ui.mobileOAuth
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpIntent
import com.quizzgame.shared.auth.presentation.OtpState
import com.quizzgame.shared.auth.presentation.OtpStep

@Composable
internal fun MobileSignInScreen(onCancel: () -> Unit, recovery: Boolean = false) {
    val model: MobileSignInViewModel = hiltViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    val oauth = mobileOAuth()
    val visible = state.withProviders(oauth.methods)
    val providerActive = oauth.status != MobileOAuthStatus.IDLE
    MobileSignInContent(
        visible.copy(
            step = if (oauth.status == MobileOAuthStatus.COMPLETED) OtpStep.Finished else visible.step,
            busy = state.busy || providerActive,
            failure = oauth.failure ?: state.failure,
        ),
        { intent ->
            when {
                providerActive -> Unit
                intent is OtpIntent.Choose && intent.method in oauth.methods -> oauth.start(intent.method)
                else -> {
                    if (oauth.failure != null) oauth.cancel()
                    model.onIntent(intent)
                }
            }
        },
        onCancel,
        recovery,
    )
}

private fun OtpState.withProviders(providers: Set<AuthMethod>): OtpState {
    val methods = step as? OtpStep.Methods ?: return this
    return copy(
        step = methods.copy(
            choices = methods.choices.filter {
                it.usesOtp || it in providers
            },
        ),
    )
}
