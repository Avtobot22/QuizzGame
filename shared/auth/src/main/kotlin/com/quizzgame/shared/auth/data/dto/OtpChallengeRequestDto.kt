package com.quizzgame.shared.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal class OtpChallengeRequestDto(val method: String, val identifier: String) {
    override fun toString(): String = "OtpChallengeRequestDto(<redacted>)"
}
