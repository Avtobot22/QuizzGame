package com.quizzgame.core.error

import com.quizzgame.core.session.SessionState
import com.quizzgame.core.session.SessionStorageFailure
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionErrorClassifierTest {

    private val classifier: ErrorClassifier = DefaultErrorClassifier()

    @Test
    fun `storage failures retain cause regardless of last known session state`() {
        val cases = mapOf(
            SessionStorageFailure.READ to StorageKind.READ,
            SessionStorageFailure.WRITE to StorageKind.WRITE,
            SessionStorageFailure.CRYPTO to StorageKind.CRYPTO,
            SessionStorageFailure.CORRUPT to StorageKind.CORRUPT,
        )
        assertEquals(SessionStorageFailure.entries.toSet(), cases.keys)
        val lastKnownStates = listOf(null, SessionState.Guest, SessionState.Authenticated(73), SessionState.Expired)
        cases.forEach { (reason, kind) ->
            lastKnownStates.forEach { lastKnown ->
                assertEquals(
                    "Storage $reason after $lastKnown",
                    AppFailure.Storage(kind),
                    classifier.fromSession(SessionState.Unavailable(reason, lastKnown)),
                )
            }
        }
    }
}
