package com.quizzgame.feature.auth.tv.presentation

import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpCodeInput

internal sealed interface TvSignInStep {
    data object Loading : TvSignInStep
    data class Methods(val otp: List<AuthMethod>, val google: Boolean) : TvSignInStep

    data class Identifier(val method: AuthMethod, val input: String = "") : TvSignInStep {
        override fun toString(): String = "Identifier(<redacted>)"
    }

    data class Code(
        val method: AuthMethod,
        val input: String,
        val form: OtpCodeInput,
    ) : TvSignInStep {
        override fun toString(): String = "Code(<redacted>)"
    }

    data object Finished : TvSignInStep
}
