package com.quizzgame.shared.auth.data.dto.device

import kotlinx.serialization.Serializable

@Serializable
internal data class DeviceAuthStartDto(
    val requestId: String,
    val userCode: String,
    val verificationUri: String,
    val verificationUriComplete: String,
    val deviceSecret: String,
    val expiresAt: String,
    val pollIntervalSeconds: Long,
) {
    override fun toString() = "DeviceAuthStartDto(<redacted>)"
}
