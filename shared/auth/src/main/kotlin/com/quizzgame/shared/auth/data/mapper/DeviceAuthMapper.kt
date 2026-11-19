package com.quizzgame.shared.auth.data.mapper

import com.quizzgame.shared.auth.data.dto.device.DeviceAuthStartDto
import com.quizzgame.shared.auth.data.dto.device.DeviceAuthStatusDto
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthRequest
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthStatus
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthorization
import java.net.URI
import java.time.Instant

internal fun DeviceAuthStartDto.toDomain(verificationHost: String): DeviceAuthRequest {
    require(requestId.matches(Regex("[A-Za-z0-9_-]{1,128}")) && deviceSecret.isNotBlank())
    require(userCode.matches(Regex("[A-Za-z0-9-]{4,24}")) && pollIntervalSeconds >= MIN_POLL_SECONDS)
    val uri = validateVerificationUri(verificationUri, verificationHost, deviceSecret)
    val completeUri = validateVerificationUri(verificationUriComplete, verificationHost, deviceSecret)
    val expiry = requireNotNull(runCatching { Instant.parse(expiresAt) }.getOrNull())
    return DeviceAuthRequest(
        requestId,
        deviceSecret,
        DeviceAuthorization(userCode, uri.toASCIIString(), completeUri.toASCIIString()),
        expiry,
        pollIntervalSeconds,
    )
}

internal fun DeviceAuthStatusDto.toDomain(): DeviceAuthStatus =
    requireNotNull(DeviceAuthStatus.entries.firstOrNull { it.name.lowercase() == status })

private fun validateVerificationUri(value: String, expectedHost: String, secret: String): URI {
    val uri = requireNotNull(runCatching { URI(value) }.getOrNull())
    require(uri.scheme == "https" && expectedHost.equals(uri.host, ignoreCase = true))
    require(uri.userInfo == null && uri.fragment == null && uri.port == -1)
    require(uri.toASCIIString().length <= MAX_VERIFICATION_URL_LENGTH)
    require(secret !in uri.schemeSpecificPart && secret !in uri.rawSchemeSpecificPart)
    return uri
}

private const val MIN_POLL_SECONDS = 2
private const val MAX_VERIFICATION_URL_LENGTH = 512
