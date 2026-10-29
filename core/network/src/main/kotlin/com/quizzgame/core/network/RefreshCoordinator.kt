package com.quizzgame.core.network

import java.io.Closeable
import java.io.IOException

interface RefreshCoordinator {

    fun register(observed: CredentialsSnapshot): RefreshAttempt
}

interface RefreshAttempt : Closeable {

    fun refresh(): RefreshOutcome
    fun cancel()
}

sealed interface RefreshOutcome {
    class Ready(val credentials: CredentialsSnapshot) : RefreshOutcome {

        override fun toString(): String = "Ready(credentials=<redacted>)"
    }

    data object SessionUnavailable : RefreshOutcome
    data object Revoked : RefreshOutcome
    data object Cancelled : RefreshOutcome
    data class Failed(val reason: RefreshFailureReason) : RefreshOutcome
}

sealed interface RefreshFailureReason {
    data class Http(val status: Int) : RefreshFailureReason
    data object Io : RefreshFailureReason
    data object Timeout : RefreshFailureReason
    data object InvalidResponse : RefreshFailureReason
    data object Storage : RefreshFailureReason
}

class RefreshFailedException(val reason: RefreshFailureReason) : IOException("Session refresh failed: $reason")

class SessionRevokedException : IOException("Session was revoked")

internal fun CredentialsSnapshot.sameVersion(other: CredentialsSnapshot): Boolean =
    sessionGeneration == other.sessionGeneration &&
        credentialsRevision == other.credentialsRevision &&
        accessToken == other.accessToken
