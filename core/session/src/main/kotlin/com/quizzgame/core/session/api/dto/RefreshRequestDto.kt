package com.quizzgame.core.session.api.dto

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
internal class RefreshRequestDto(val refreshToken: String, val deviceId: String) {
    init {
        require(refreshToken.isNotBlank()) { "Refresh token must not be blank" }
        val canonicalId = try {
            UUID.fromString(deviceId).toString()
        } catch (_: IllegalArgumentException) {
            null
        }
        require(canonicalId == deviceId) { "Device ID must be a canonical UUID" }
    }

    override fun toString(): String = "RefreshRequestDto(refreshToken=<redacted>, deviceId=<redacted>)"
}
