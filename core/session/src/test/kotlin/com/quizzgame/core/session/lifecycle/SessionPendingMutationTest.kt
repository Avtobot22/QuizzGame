package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.storage.SessionIntent
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageFailure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPendingMutationTest {
    @Test
    fun failedSignInFencesOldCredentialsAndRecoveryPersistsExactlyTheNewPair() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            val previous = manager.snapshot()
            store.failBefore[1] = SessionStorageFailure.WRITE
            assertEquals(SessionOperation.Failed(SessionStorageFailure.WRITE), manager.startSession(fixturePair("B")))
            assertEquals("fixture-A-access", store.durable.tokens!!.access)
            assertBlocked(manager)
            assertEquals(SessionOperation.Busy, manager.restoreStoredSession())
            assertEquals(SessionRecoveryAction.RETRY_PERSISTENCE, manager.recoveryAction.value)
            assertEquals(SessionRecoveryResult.Recovered, manager.recoverStorage())
            assertEquals("fixture-B-access", manager.snapshot().accessToken)
            assertTrue(manager.snapshot().sessionGeneration > previous.sessionGeneration)
            assertEquals(SessionIntent.NONE, store.durable.intent)
        }
    }

    @Test
    fun refreshWriteFailureKeepsBPrivateUntilLocalRecoveryAndNeverRestoresA() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            val observed = manager.snapshot()
            assertEquals(SessionOperation.Applied, manager.beginRefreshBlocking(observed))
            store.failBefore[2] = SessionStorageFailure.WRITE
            assertEquals(
                SessionOperation.Failed(SessionStorageFailure.WRITE),
                manager.applyRefreshedTokensIfCurrentBlocking(observed, fixturePair("B")),
            )
            assertEquals(SessionIntent.REFRESH, store.durable.intent)
            assertBlocked(manager)
            assertNull(manager.refreshState)
            assertEquals(SessionRecoveryAction.RETRY_PERSISTENCE, manager.recoveryAction.value)
            assertEquals(SessionRecoveryResult.Recovered, manager.recoverStorage())
            assertEquals("fixture-B-access", manager.snapshot().accessToken)
            assertEquals(observed.sessionGeneration, manager.snapshot().sessionGeneration)
            assertEquals(SessionIntent.NONE, store.durable.intent)
            assertEquals(SessionOperation.Stale, manager.beginRefreshBlocking(observed))
        }
    }

    @Test
    fun writeWhichCommittedBeforeThrowingIsReconciledWithoutAnotherWrite() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            val observed = manager.snapshot()
            manager.beginRefreshBlocking(observed)
            store.failAfter[2] = SessionStorageFailure.WRITE
            manager.applyRefreshedTokensIfCurrentBlocking(observed, fixturePair("B"))
            assertEquals("fixture-B-access", store.durable.tokens!!.access)
            assertBlocked(manager)
            val writes = store.writes
            assertEquals(SessionRecoveryResult.Recovered, manager.recoverStorage())
            assertEquals(writes, store.writes)
            assertEquals("fixture-B-access", manager.snapshot().accessToken)
        }
    }

    @Test
    fun durableLogoutMarkerCompletesClearOnColdStartBeforePublishingGuest() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            store.failBefore[2] = SessionStorageFailure.WRITE
            assertEquals(SessionOperation.Failed(SessionStorageFailure.WRITE), manager.logout())
            assertTrue(manager.state.value is SessionState.Unavailable)
            assertEquals(SessionIntent.LOGOUT, store.durable.intent)
            assertBlocked(manager)
        }
        SessionManager.open(SessionFaultStore(store.durable)).use { restarted ->
            assertEquals(SessionState.Guest, restarted.state.value)
            assertNull(restarted.snapshot().accessToken)
        }
    }

    @Test
    fun confirmedExpiryPublishesExpiredOnlyAfterItsPendingClearIsDurable() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            val observed = manager.snapshot()
            store.failBefore[2] = SessionStorageFailure.WRITE
            assertEquals(
                SessionOperation.Failed(SessionStorageFailure.WRITE),
                manager.expireIfCurrentBlocking(observed),
            )
            assertTrue(manager.state.value is SessionState.Unavailable)
            assertEquals(SessionIntent.REVOKED, store.durable.intent)
            assertBlocked(manager)
            assertEquals(SessionRecoveryResult.Recovered, manager.recoverStorage())
            assertEquals(SessionState.Expired, manager.state.value)
            assertNull(manager.snapshot().accessToken)
        }
    }

    @Test
    fun interruptedAccountSwitchCannotRestoreThePreviousAccountAfterRestart() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            store.failBefore[2] = SessionStorageFailure.WRITE
            manager.startSession(fixturePair("B"))
            assertEquals(SessionIntent.LOGOUT, store.durable.intent)
            assertEquals("fixture-A-access", store.durable.tokens!!.access)
        }
        SessionManager.open(SessionFaultStore(store.durable)).use { restarted ->
            assertEquals(SessionState.Guest, restarted.state.value)
            assertNull(restarted.snapshot().accessToken)
        }
    }

    @Test
    fun reconciliationNeverOverwritesAnUnexpectedDurableSession() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            store.failBefore[1] = SessionStorageFailure.WRITE
            manager.startSession(fixturePair("B"))
            store.durable = SessionRecord(TEST_DEVICE_ID, fixturePair("unexpected"), epoch = 9, revision = 9)
            val writes = store.writes
            assertEquals(
                SessionRecoveryResult.NeedsStorageRecovery(SessionStorageFailure.CORRUPT),
                manager.recoverStorage(),
            )
            assertEquals(writes, store.writes)
            assertEquals(SessionRecoveryAction.NONE, manager.recoveryAction.value)
            assertBlocked(manager)
        }
    }

    @Test
    fun cryptoAndCorruptionFailuresNeverEraseOrBlindlyRetryTheStore() = runTest {
        listOf(SessionStorageFailure.CRYPTO, SessionStorageFailure.CORRUPT).forEach { failure ->
            val store = SessionFaultStore().apply { readFailure = failure }
            SessionManager.open(store).use { manager ->
                assertEquals(SessionRecoveryAction.NONE, manager.recoveryAction.value)
                assertEquals(SessionRecoveryResult.NotAllowed, manager.recoverStorage())
                assertEquals(SessionOperation.Busy, manager.restoreStoredSession())
                assertEquals(0, store.writes)
                assertEquals("fixture-A-access", store.durable.tokens!!.access)
            }
        }
    }
}

private fun assertBlocked(manager: SessionManager) {
    val failure = assertThrows(SessionUnavailableException::class.java) { manager.snapshot() }
    assertEquals(CredentialUnavailableKind.STORAGE_UNAVAILABLE, failure.kind)
    assertNull(manager.refreshState)
}
