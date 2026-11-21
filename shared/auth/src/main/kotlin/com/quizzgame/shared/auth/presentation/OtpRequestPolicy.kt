package com.quizzgame.shared.auth.presentation

import com.quizzgame.shared.auth.domain.entity.AuthFailure

/** Правила ввода и ограничения запросов для формы OTP. */
object OtpRequestPolicy {
    private const val MAX_IDENTIFIER_LENGTH = 254

    fun identifier(value: String): String = value.take(MAX_IDENTIFIER_LENGTH)

    fun canRequest(busy: Boolean, failure: AuthFailure?, blockedUntil: Long, elapsed: Long): Boolean =
        !busy && elapsed >= blockedUntil && failure != AuthFailure.StorageRecovery

    fun identifierFailure(failure: AuthFailure?, blockedUntil: Long, elapsed: Long): AuthFailure? =
        failure ?: AuthFailure.RateLimited(null).takeIf { elapsed < blockedUntil }

    fun afterEdit(failure: AuthFailure?): AuthFailure? =
        failure.takeUnless { it == AuthFailure.InvalidCode || it == AuthFailure.InvalidIdentifier }
}
