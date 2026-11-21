package com.quizzgame.feature.auth.mobile.presentation

import com.quizzgame.shared.auth.domain.entity.AuthMethod

internal sealed interface MobileSignInIntent {
    data object Load : MobileSignInIntent
    data class ChooseOtp(val method: AuthMethod) : MobileSignInIntent
    class Edit(val value: String) : MobileSignInIntent {
        override fun toString(): String = "Edit(<redacted>)"
    }
    data object RequestCode : MobileSignInIntent
    data object Verify : MobileSignInIntent
    data object Back : MobileSignInIntent
}
