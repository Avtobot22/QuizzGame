package com.quizzgame.core.network.auth

import java.io.IOException

enum class CredentialUnavailableKind {
    NOT_INITIALIZED,
    STORAGE_UNAVAILABLE,
    TERMINATING,
    REFRESH_UNCERTAIN,
    CLOSED,
}

open class CredentialUnavailableException(val kind: CredentialUnavailableKind) :
    IOException("Session credentials are unavailable: $kind")
