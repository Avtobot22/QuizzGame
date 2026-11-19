package com.quizzgame.shared.auth.domain.entity.device

import java.time.Instant

/** Только эти данные показаны на экране входа и используются для QR. */
data class DeviceAuthorization(val code: String, val verificationUri: String, val qrUri: String) {

    override fun toString() = "DeviceAuthorization(<redacted>)"
}

internal class DeviceAuthRequest(
    val id: String,
    val secret: String,
    val display: DeviceAuthorization,
    val expiresAt: Instant,
    val pollIntervalSeconds: Long,
) {

    override fun toString() = "DeviceAuthRequest(<redacted>)"
}

internal enum class DeviceAuthStatus {
    PENDING,
    APPROVED,
    DENIED,
    EXPIRED,
}
