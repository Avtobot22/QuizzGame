package com.quizzgame.shared.auth.domain.entity

sealed interface OtpIntent {
    data object Load : OtpIntent
    data class Choose(val method: AuthMethod) : OtpIntent
    class Edit(val value: String) : OtpIntent {
        override fun toString(): String = "Edit(<redacted>)"
    }
    data object Send : OtpIntent
    data object Verify : OtpIntent
    data object Resend : OtpIntent
    data object Back : OtpIntent
}
