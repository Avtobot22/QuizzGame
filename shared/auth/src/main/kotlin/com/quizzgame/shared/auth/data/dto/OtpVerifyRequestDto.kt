package com.quizzgame.shared.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal class OtpVerifyRequestDto(val code: String, val deviceId: String) {
    override fun toString(): String = "OtpVerifyRequestDto(<redacted>)"
}
