package com.quizzgame.shared.auth.presentation

import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.AuthMethod

data class OtpState(
    val step: OtpStep = OtpStep.Loading,
    val busy: Boolean = false,
    val failure: AuthFailure? = null,
    val requestBlockedUntil: Long = 0,
) {
    val methodsUnavailable: Boolean
        get() = when (step) {
            OtpStep.Loading    -> failure != null
            is OtpStep.Methods -> step.choices.isEmpty()
            else               -> false
        }

    fun canRequest(elapsed: Long): Boolean =
        !busy && elapsed >= requestBlockedUntil && failure != AuthFailure.StorageRecovery

    fun identifierFailure(elapsed: Long): AuthFailure? =
        failure ?: AuthFailure.RateLimited(null).takeIf { elapsed < requestBlockedUntil }

    fun isCodeExpired(code: OtpStep.Code, elapsed: Long): Boolean =
        elapsed >= code.expiresAtMillis || failure == AuthFailure.Expired || failure == AuthFailure.Consumed

    fun resendWaitMillis(code: OtpStep.Code, elapsed: Long): Long =
        (maxOf(requestBlockedUntil, code.resendAtMillis) - elapsed).coerceAtLeast(0)

    internal fun chooseOtpMethod(method: AuthMethod): OtpState {
        if (step !is OtpStep.Methods || method !in step.choices || !method.usesOtp) return this
        return copy(step = OtpStep.Identifier(method), failure = null)
    }

    internal fun withInput(value: String): OtpState {
        val edited = when (step) {
            is OtpStep.Identifier -> step.copy(input = value.take(MAX_IDENTIFIER_LENGTH))
            is OtpStep.Code       -> step.copy(code = value.filter { it in '0'..'9' }.take(step.challenge.codeLength))
            else                  -> return this
        }
        return copy(
            step = edited,
            failure = failure.takeUnless { it == AuthFailure.InvalidCode || it == AuthFailure.InvalidIdentifier },
        )
    }

    private companion object {
        const val MAX_IDENTIFIER_LENGTH = 254
    }
}
