package com.quizzgame.shared.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal class GoogleVerifyRequestDto(val challengeId: String, val idToken: String, val deviceId: String) {
    override fun toString(): String = "GoogleVerifyRequestDto(<redacted>)"
}
