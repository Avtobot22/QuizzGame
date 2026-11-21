package com.quizzgame.shared.auth.presentation

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.otp.OtpChallenge

/** Поле OTP со своим форматом и сроками действия. */
data class OtpCodeInput(
    val challenge: OtpChallenge,
    val expiresAtMillis: Long,
    val resendAtMillis: Long,
    val code: String = "",
) {
    val complete: Boolean get() = code.length == challenge.codeLength

    fun isExpired(elapsed: Long, failure: AuthFailure?): Boolean =
        elapsed >= expiresAtMillis || failure == AuthFailure.Expired || failure == AuthFailure.Consumed

    fun resendWaitMillis(blockedUntil: Long, elapsed: Long): Long =
        (maxOf(blockedUntil, resendAtMillis) - elapsed).coerceAtLeast(0)

    fun withCode(value: String): OtpCodeInput = copy(code = value.filter { it in '0'..'9' }.take(challenge.codeLength))

    override fun toString(): String = "OtpCodeInput(<redacted>)"
}
