package com.quizzgame.feature.dashboard.mobile.domain.entity

internal sealed interface Account {
    data object Loading : Account
    data object Guest : Account
    data class SignedIn(val displayName: String?, val profileUnavailable: Boolean = false) : Account {
        override fun toString(): String = "SignedIn(<redacted>)"
    }
    data object Unavailable : Account
}
