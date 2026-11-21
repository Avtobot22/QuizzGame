package com.quizzgame.feature.auth.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInState
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInStep
import com.quizzgame.feature.auth.mobile.presentation.MobileSignInViewModel
import com.quizzgame.feature.authprovider.mobile.ui.MobileOAuthStatus
import com.quizzgame.feature.authprovider.mobile.ui.mobileOAuth
import com.quizzgame.shared.auth.domain.entity.AuthMethod

@Composable
internal fun MobileSignInScreen(onCancel: () -> Unit, recovery: Boolean = false) {
    val model: MobileSignInViewModel = hiltViewModel()
    val state by model.state.collectAsStateWithLifecycle()
    val oauth = mobileOAuth()
    val visible = state.withProviders(oauth.methods)
    val providerActive = oauth.status != MobileOAuthStatus.IDLE
    MobileSignInContent(
        visible.copy(
            step = if (oauth.status == MobileOAuthStatus.COMPLETED) MobileSignInStep.Finished else visible.step,
            busy = state.busy || providerActive,
            failure = oauth.failure ?: state.failure,
        ),
        onIntent = { intent ->
            if (!providerActive) {
                if (oauth.failure != null) oauth.cancel()
                model.onIntent(intent)
            }
        },
        onProvider = { method ->
            if (!providerActive && method in oauth.methods) oauth.start(method)
        },
        onCancel = onCancel,
        recovery = recovery,
    )
}

private fun MobileSignInState.withProviders(providers: Set<AuthMethod>): MobileSignInState {
    val methods = step as? MobileSignInStep.Methods ?: return this
    return copy(
        step = methods.copy(providers = methods.providers.filter { it in providers }),
    )
}
