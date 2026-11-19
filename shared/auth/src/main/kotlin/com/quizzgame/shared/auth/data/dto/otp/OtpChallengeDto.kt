package com.quizzgame.shared.auth.data.dto.otp

import kotlinx.serialization.Serializable

@Serializable
internal class OtpChallengeDto(
    val challengeId: String,
    val maskedDestination: String,
    val codeLength: Int,
    val expiresAt: String,
    val resendAfterSeconds: Long,
    val attemptsRemaining: Int? = null,
)
