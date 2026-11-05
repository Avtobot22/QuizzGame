package com.quizzgame.core.navigation

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState

enum class GuestAccess { UNKNOWN, ALLOWED, DENIED }

enum class RootNavigationDecision { BOOTSTRAP, SPLASH, CONTENT, SIGN_IN, BLOCKED }

fun decideRootNavigation(session: SessionState, guestAccess: GuestAccess): RootNavigationDecision = when (session) {
    SessionState.Initializing -> RootNavigationDecision.BOOTSTRAP
    SessionState.Closed, is SessionState.Unavailable -> RootNavigationDecision.BLOCKED
    is SessionState.Blocked -> if (session.kind == CredentialUnavailableKind.REFRESH_UNCERTAIN) {
        RootNavigationDecision.SIGN_IN
    } else {
        RootNavigationDecision.BLOCKED
    }
    SessionState.Expired -> RootNavigationDecision.SIGN_IN
    is SessionState.Authenticated -> RootNavigationDecision.CONTENT
    SessionState.Guest -> when (guestAccess) {
        GuestAccess.UNKNOWN -> RootNavigationDecision.SPLASH
        GuestAccess.ALLOWED -> RootNavigationDecision.CONTENT
        GuestAccess.DENIED -> RootNavigationDecision.SIGN_IN
    }
}

fun sessionNavigationEpoch(session: SessionState): String = when (session) {
    is SessionState.Authenticated -> "authenticated:${session.sessionGeneration}"
    SessionState.Guest -> "guest"
    SessionState.Expired -> "expired"
    SessionState.Initializing -> "initializing"
    SessionState.Closed -> "closed"
    is SessionState.Unavailable -> "unavailable"
    is SessionState.Blocked -> "blocked:${session.kind}"
}
