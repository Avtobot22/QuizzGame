package com.quizzgame.core.error

import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.session.SessionState
import com.quizzgame.core.session.SessionStorageFailure

class DefaultErrorClassifier : ErrorClassifier {

    override fun fromNetwork(failure: NetworkFailure): AppFailure = classifyNetworkFailure(failure)

    override fun fromSession(state: SessionState.Unavailable): AppFailure = AppFailure.Storage(
        when (state.reason) {
            SessionStorageFailure.READ    -> StorageKind.READ
            SessionStorageFailure.WRITE   -> StorageKind.WRITE
            SessionStorageFailure.CRYPTO  -> StorageKind.CRYPTO
            SessionStorageFailure.CORRUPT -> StorageKind.CORRUPT
        },
    )
}
