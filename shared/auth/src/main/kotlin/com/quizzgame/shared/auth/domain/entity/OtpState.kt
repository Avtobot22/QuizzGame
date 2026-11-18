package com.quizzgame.shared.auth.domain.entity

data class OtpState(
    val step: OtpStep = OtpStep.Loading,
    val busy: Boolean = false,
    val failure: AuthFailure? = null,
    val requestBlockedUntil: Long = 0,
)
