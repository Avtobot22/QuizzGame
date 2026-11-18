package com.quizzgame.feature.dashboard.mobile.domain.entity

internal sealed interface Account {
    data object Loading : Account
    data object Guest : Account
    data class SignedIn(val displayName: String?) : Account {
        override fun toString(): String = "SignedIn(<redacted>)"
    }
    data object Unavailable : Account
}
