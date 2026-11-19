package com.quizzgame.shared.auth.domain.entity.otp

import java.time.Instant

class OtpChallenge(
    val id: String,
    val maskedDestination: String,
    val codeLength: Int,
    val expiresAt: Instant,
    val resendAfterSeconds: Long,
    val attemptsRemaining: Int?,
) {
    override fun toString(): String = "OtpChallenge(<redacted>)"
}
