package com.quizzgame.shared.auth.data.dto.provider

import kotlinx.serialization.Serializable

@Serializable
internal class YandexRedeemRequestDto(val attemptSecret: String, val deviceId: String) {
    override fun toString(): String = "YandexRedeemRequestDto(<redacted>)"
}
