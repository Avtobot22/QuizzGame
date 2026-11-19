package com.quizzgame.shared.auth.data.mapper

import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.shared.auth.data.dto.TokenPairDto
import com.quizzgame.shared.auth.data.dto.methods.AuthMethodsDto
import com.quizzgame.shared.auth.data.dto.otp.OtpChallengeDto
import com.quizzgame.shared.auth.data.dto.provider.GoogleChallengeDto
import com.quizzgame.shared.auth.data.dto.provider.YandexAttemptDto
import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge
import com.quizzgame.shared.auth.domain.entity.provider.GoogleChallenge
import com.quizzgame.shared.auth.domain.entity.provider.YandexAttempt
import java.net.URI
import java.time.Instant

internal fun AuthMethodsDto.toDomain(): List<AuthMethod> {
    require(methods.all { it.displayName.isNotBlank() })
    require(methods.map { it.id }.distinct().size == methods.size)
    return AuthMethod.entries.filter { method -> methods.any { it.enabled && it.id == method.wireId } }
}

internal fun TokenPairDto.toDomain(): TokenPair {
    require(access.isNotBlank() && refresh.isNotBlank())
    return TokenPair(access, refresh)
}

internal fun OtpChallengeDto.toDomain(): OtpChallenge {
    require(challengeId.isNotBlank() && maskedDestination.isNotBlank())
    require(codeLength in MIN_CODE_LENGTH..MAX_CODE_LENGTH && resendAfterSeconds >= 0 && (attemptsRemaining ?: 0) >= 0)
    return OtpChallenge(
        challengeId,
        maskedDestination,
        codeLength,
        parseInstant(expiresAt),
        resendAfterSeconds,
        attemptsRemaining,
    )
}

internal fun GoogleChallengeDto.toDomain(): GoogleChallenge {
    require(challengeId.isNotBlank() && nonce.length >= MIN_NONCE_LENGTH)
    return GoogleChallenge(challengeId, nonce, parseInstant(expiresAt))
}

internal fun YandexAttemptDto.toDomain(): YandexAttempt {
    require(attemptId.matches(Regex("[A-Za-z0-9_-]{1,128}")) && attemptSecret.isNotBlank())
    val uri = requireNotNull(runCatching { URI(authorizationUrl) }.getOrNull())
    require(uri.scheme == "https" && uri.host == "oauth.yandex.ru" && uri.path == "/authorize")
    require(uri.port == -1 && uri.userInfo == null && uri.fragment == null)
    return YandexAttempt(attemptId, uri, attemptSecret, parseInstant(expiresAt))
}

private fun parseInstant(value: String): Instant = requireNotNull(runCatching { Instant.parse(value) }.getOrNull())

private const val MIN_CODE_LENGTH = 4
private const val MAX_CODE_LENGTH = 10
private const val MIN_NONCE_LENGTH = 12
