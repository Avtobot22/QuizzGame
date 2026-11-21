package com.quizzgame.feature.auth.mobile.presentation

import com.quizzgame.shared.auth.domain.entity.AuthMethod
import com.quizzgame.shared.auth.presentation.OtpCodeInput

internal sealed interface MobileSignInStep {
    data object Loading : MobileSignInStep
    data class Methods(val otp: List<AuthMethod>, val providers: List<AuthMethod>) : MobileSignInStep

    data class Identifier(val method: AuthMethod, val input: String = "") : MobileSignInStep {
        override fun toString(): String = "Identifier(<redacted>)"
    }

    data class Code(
        val method: AuthMethod,
        val input: String,
        val form: OtpCodeInput,
    ) : MobileSignInStep {
        override fun toString(): String = "Code(<redacted>)"
    }

    data object Finished : MobileSignInStep
}
