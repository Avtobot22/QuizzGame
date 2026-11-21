package com.quizzgame.feature.auth.tv.presentation

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.presentation.OtpRequestPolicy

internal data class TvSignInState(
    val step: TvSignInStep = TvSignInStep.Loading,
    val busy: Boolean = false,
    val failure: AuthFailure? = null,
    val requestBlockedUntil: Long = 0,
) {
    val methodsUnavailable: Boolean
        get() = when (step) {
            TvSignInStep.Loading    -> failure != null
            is TvSignInStep.Methods -> step.otp.isEmpty() && !step.google
            else               -> false
        }

    fun canRequest(elapsed: Long): Boolean =
        OtpRequestPolicy.canRequest(busy, failure, requestBlockedUntil, elapsed)

    fun identifierFailure(elapsed: Long): AuthFailure? =
        OtpRequestPolicy.identifierFailure(failure, requestBlockedUntil, elapsed)
}
