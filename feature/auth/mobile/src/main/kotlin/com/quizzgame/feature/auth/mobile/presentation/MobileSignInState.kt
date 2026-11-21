package com.quizzgame.feature.auth.mobile.presentation

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.presentation.OtpRequestPolicy

internal data class MobileSignInState(
    val step: MobileSignInStep = MobileSignInStep.Loading,
    val busy: Boolean = false,
    val failure: AuthFailure? = null,
    val requestBlockedUntil: Long = 0,
) {
    val methodsUnavailable: Boolean
        get() = when (step) {
            MobileSignInStep.Loading    -> failure != null
            is MobileSignInStep.Methods -> step.otp.isEmpty() && step.providers.isEmpty()
            else               -> false
        }

    fun canRequest(elapsed: Long): Boolean =
        OtpRequestPolicy.canRequest(busy, failure, requestBlockedUntil, elapsed)

    fun identifierFailure(elapsed: Long): AuthFailure? =
        OtpRequestPolicy.identifierFailure(failure, requestBlockedUntil, elapsed)
}
