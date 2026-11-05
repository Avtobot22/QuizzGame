package com.quizzgame.core.error.classification

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.AuthenticationKind
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.network.auth.CredentialUnavailableKind

internal fun classifyCredentialFailure(kind: CredentialUnavailableKind): AppFailure = when (kind) {
    CredentialUnavailableKind.STORAGE_UNAVAILABLE -> AppFailure.Storage(StorageKind.UNKNOWN)
    CredentialUnavailableKind.REFRESH_UNCERTAIN,
    -> AppFailure.Authentication(AuthenticationKind.RECOVERY_REQUIRED)
    CredentialUnavailableKind.NOT_INITIALIZED,
    CredentialUnavailableKind.TERMINATING,
    CredentialUnavailableKind.CLOSED,
    -> AppFailure.Authentication(AuthenticationKind.REQUIRED)
}
