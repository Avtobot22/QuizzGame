package com.quizzgame.app.startup

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageFailure

sealed interface StorageRecoveryDecision {
    data object RetryRead : StorageRecoveryDecision
    data object RetryPersistence : StorageRecoveryDecision
    data object UnavailableWithoutAutomaticRecovery : StorageRecoveryDecision
}

class StorageRecoveryPolicy {
    internal fun allowsRetry(state: SessionState?): Boolean =
        state !is SessionState.Unavailable ||
            decide(state.reason) != StorageRecoveryDecision.UnavailableWithoutAutomaticRecovery

    fun decide(reason: SessionStorageFailure): StorageRecoveryDecision = when (reason) {
        SessionStorageFailure.READ -> StorageRecoveryDecision.RetryRead
        SessionStorageFailure.WRITE -> StorageRecoveryDecision.RetryPersistence
        SessionStorageFailure.CRYPTO,
        SessionStorageFailure.CORRUPT,
        -> StorageRecoveryDecision.UnavailableWithoutAutomaticRecovery
    }
}
