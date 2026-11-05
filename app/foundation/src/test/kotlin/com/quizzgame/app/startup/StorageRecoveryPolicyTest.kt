package com.quizzgame.app.startup

import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.session.storage.SessionStorageFailure
import org.junit.Assert.assertEquals
import org.junit.Test

class StorageRecoveryPolicyTest {
    @Test
    fun storageRetryDistinguishesReadAndPendingPersistence() {
        val recovery = StorageRecoveryPolicy()
        SessionStorageFailure.entries.forEach { reason ->
            val expected = when (reason) {
                SessionStorageFailure.READ -> StorageRecoveryDecision.RetryRead
                SessionStorageFailure.WRITE -> StorageRecoveryDecision.RetryPersistence
                else -> StorageRecoveryDecision.UnavailableWithoutAutomaticRecovery
            }
            assertEquals(expected, recovery.decide(reason))
            val failure = startupFailureState(AppStartupFailure.Storage(reason), recovery)
            assertEquals(ErrorCopyKey.STORAGE_PROBLEM, failure.error.title)
            val actions = if (reason == SessionStorageFailure.READ || reason == SessionStorageFailure.WRITE) {
                listOf(ErrorActionKind.RETRY)
            } else {
                emptyList()
            }
            assertEquals(actions, failure.error.actions)
        }
    }
}
