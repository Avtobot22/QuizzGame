package com.quizzgame.shared.auth.data.dto.provider

import kotlinx.serialization.Serializable

@Serializable
internal class GoogleChallengeDto(val challengeId: String, val nonce: String, val expiresAt: String) {
    override fun toString(): String = "GoogleChallengeDto(<redacted>)"
}
