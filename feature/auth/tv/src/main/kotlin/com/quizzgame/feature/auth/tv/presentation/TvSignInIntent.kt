package com.quizzgame.feature.auth.tv.presentation

import com.quizzgame.shared.auth.domain.entity.AuthMethod

internal sealed interface TvSignInIntent {
    data object Load : TvSignInIntent
    data class ChooseOtp(val method: AuthMethod) : TvSignInIntent
    class Edit(val value: String) : TvSignInIntent {
        override fun toString(): String = "Edit(<redacted>)"
    }
    data object RequestCode : TvSignInIntent
    data object Verify : TvSignInIntent
    data object Back : TvSignInIntent
}
