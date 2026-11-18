package com.quizzgame.shared.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal class YandexRedeemRequestDto(val attemptSecret: String, val deviceId: String) {
    override fun toString(): String = "YandexRedeemRequestDto(<redacted>)"
}
