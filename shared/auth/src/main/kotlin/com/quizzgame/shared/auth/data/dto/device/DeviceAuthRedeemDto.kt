package com.quizzgame.shared.auth.data.dto.device

import kotlinx.serialization.Serializable

@Serializable
internal data class DeviceAuthRedeemDto(val deviceSecret: String, val deviceId: String) {
    override fun toString() = "DeviceAuthRedeemDto(<redacted>)"
}
