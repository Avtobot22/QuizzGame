package com.quizzgame.shared.auth.domain.entity

sealed interface OtpStep {
    data object Loading : OtpStep
    data class Methods(val choices: List<AuthMethod>) : OtpStep

    data class Identifier(val method: AuthMethod, val input: String = "") : OtpStep {
        override fun toString(): String = "Identifier(<redacted>)"
    }

    data class Code(
        val method: AuthMethod,
        val input: String,
        val challenge: OtpChallenge,
        val expiresAtMillis: Long,
        val resendAtMillis: Long,
        val code: String = "",
    ) : OtpStep {
        override fun toString(): String = "Code(<redacted>)"
    }

    data object Finished : OtpStep
}
