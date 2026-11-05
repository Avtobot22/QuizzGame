package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.storage.SessionIntent
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageFailure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class SessionRefreshIntentTest {
    @Test
    fun refreshIntentIsDurableBeforeHttpAndUnknownOutcomeBlocksTheSamePairAcrossRestart() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            val observed = manager.snapshot()
            assertEquals(SessionOperation.Applied, manager.beginRefreshBlocking(observed))
            assertEquals(SessionIntent.REFRESH, store.durable.intent)
            assertEquals(observed.accessToken, manager.snapshot().accessToken)
            assertEquals(SessionOperation.Busy, manager.beginRefreshBlocking(observed))
            assertEquals(SessionOperation.Applied, manager.markRefreshUncertainIfCurrentBlocking(observed))
            assertEquals(
                CredentialUnavailableKind.REFRESH_UNCERTAIN,
                assertThrows(SessionUnavailableException::class.java) { manager.snapshot() }.kind,
            )
            assertNull(manager.refreshState)
            assertEquals(TEST_DEVICE_ID, manager.deviceIdProvider.get())
            assertEquals(SessionRecoveryAction.REAUTHENTICATE, manager.recoveryAction.value)
            assertEquals(SessionRecoveryResult.ReauthenticationRequired, manager.recoverStorage())
            assertEquals(SessionOperation.Busy, manager.restoreStoredSession())
        }
        SessionManager.open(SessionFaultStore(store.durable)).use { restarted ->
            assertEquals(SessionRecoveryAction.REAUTHENTICATE, restarted.recoveryAction.value)
            assertThrows(SessionUnavailableException::class.java) { restarted.snapshot() }
        }
    }

    @Test
    fun knownRejectedRequestClearsOnlyTheIntentAndRetainsCredentials() = runTest {
        val store = SessionFaultStore(
            SessionRecord(TEST_DEVICE_ID, fixturePair("A")),
        )
        SessionManager.open(store).use { manager ->
            val observed = manager.snapshot()
            manager.beginRefreshBlocking(observed)
            assertEquals(SessionOperation.Applied, manager.finishRefreshTemporaryBlocking(observed))
            assertEquals(SessionIntent.NONE, store.durable.intent)
            assertEquals(observed.sessionGeneration, manager.snapshot().sessionGeneration)
            assertEquals(observed.credentialsRevision, manager.snapshot().credentialsRevision)
            assertEquals(SessionOperation.Applied, manager.beginRefreshBlocking(observed))
        }
    }

    @Test
    fun failedIntentCommitWhichNeverSentHttpIsSafelyReconciledWithoutRefreshingA() = runTest {
        listOf(false, true).forEach { committed ->
            val store = SessionFaultStore()
            val faults = if (committed) store.failAfter else store.failBefore
            faults[1] = SessionStorageFailure.WRITE
            SessionManager.open(store).use { manager ->
                val observed = manager.snapshot()
                assertEquals(
                    SessionOperation.Failed(SessionStorageFailure.WRITE),
                    manager.beginRefreshBlocking(observed),
                )
                assertThrows(SessionUnavailableException::class.java) { manager.snapshot() }
                assertEquals(SessionRecoveryResult.Recovered, manager.recoverStorage())
                assertEquals(SessionIntent.NONE, store.durable.intent)
                assertEquals(observed.accessToken, manager.snapshot().accessToken)
                assertEquals(if (committed) 2 else 1, store.writes)
            }
        }
    }
}
