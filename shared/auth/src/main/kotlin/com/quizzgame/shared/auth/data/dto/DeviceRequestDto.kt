package com.quizzgame.shared.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal class DeviceRequestDto(val deviceId: String) {
    override fun toString(): String = "DeviceRequestDto(<redacted>)"
}
