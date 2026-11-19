package com.quizzgame.feature.authprovider.mobile.presentation

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.provider.ProviderSignInAttempt

internal sealed interface MobileOAuthState {
    data object Idle : MobileOAuthState
    data object Busy : MobileOAuthState
    data object Committed : MobileOAuthState
    data class Error(val failure: AuthFailure) : MobileOAuthState
    data class Google(val attempt: ProviderSignInAttempt.Google) : MobileOAuthState

    sealed interface Yandex : MobileOAuthState {
        val attempt: ProviderSignInAttempt.Yandex
    }

    data class OpenBrowser(override val attempt: ProviderSignInAttempt.Yandex) : Yandex
    data class AwaitingCallback(override val attempt: ProviderSignInAttempt.Yandex) : Yandex
}

internal fun oauthFailure() = MobileOAuthState.Error(AuthFailure.General(AppFailure.OperationInterrupted))
