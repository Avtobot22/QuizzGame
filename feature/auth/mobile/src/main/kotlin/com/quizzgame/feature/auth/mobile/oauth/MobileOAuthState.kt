package com.quizzgame.feature.auth.mobile.oauth

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.GoogleChallenge
import com.quizzgame.shared.auth.domain.entity.YandexAttempt

internal sealed interface MobileOAuthState {
    data object Idle : MobileOAuthState
    data object Busy : MobileOAuthState
    data object Committed : MobileOAuthState
    data class Error(val failure: AuthFailure) : MobileOAuthState
    data class Google(val challenge: GoogleChallenge) : MobileOAuthState
    data class Yandex(val attempt: YandexAttempt, val browserOpened: Boolean = false) : MobileOAuthState
}

internal fun oauthFailure() = MobileOAuthState.Error(AuthFailure.General(AppFailure.OperationInterrupted))
