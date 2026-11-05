package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.storage.SessionStorageFailure
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SessionFencingTest {
    @Test
    fun logoutFencesSnapshotsBeforeBlockedDiskIoAndItsFailedWriteStillRejectsLateRefresh() = runTest {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val store = SessionFaultStore().apply {
            failBefore[1] = SessionStorageFailure.WRITE
            beforeWrite = {
                entered.countDown()
                check(release.await(5, TimeUnit.SECONDS))
            }
        }
        SessionManager.open(store).use { manager ->
            val observed = manager.snapshot()
            val executor = Executors.newFixedThreadPool(2)
            try {
                val logout = executor.submit<SessionOperation> { runBlocking { manager.logout() } }
                assertTrue(entered.await(5, TimeUnit.SECONDS))
                assertEquals(
                    CredentialUnavailableKind.TERMINATING,
                    assertThrows(SessionUnavailableException::class.java) { manager.snapshot() }.kind,
                )
                val late = executor.submit<SessionOperation> {
                    manager.applyRefreshedTokensIfCurrentBlocking(observed, fixturePair("late"))
                }
                release.countDown()
                assertEquals(SessionOperation.Failed(SessionStorageFailure.WRITE), logout.get(5, TimeUnit.SECONDS))
                assertEquals(SessionOperation.Stale, late.get(5, TimeUnit.SECONDS))
                assertThrows(SessionUnavailableException::class.java) { manager.snapshot() }
            } finally {
                release.countDown()
                executor.shutdownNow()
            }
        }
    }

    @Test
    fun newAccountSupersedesAnOldRefreshAndItsLateSuccessOrFailureCannotTouchB() = runTest {
        val store = SessionFaultStore()
        SessionManager.open(store).use { manager ->
            val observed = manager.snapshot()
            manager.beginRefreshBlocking(observed)
            manager.markRefreshUncertainIfCurrentBlocking(observed)
            assertEquals(SessionOperation.Applied, manager.startSession(fixturePair("B")))
            assertEquals(
                SessionOperation.Stale,
                manager.applyRefreshedTokensIfCurrentBlocking(observed, fixturePair("late")),
            )
            assertEquals(SessionOperation.Stale, manager.expireIfCurrentBlocking(observed))
            assertEquals(SessionOperation.Stale, manager.markRefreshUncertainIfCurrentBlocking(observed))
            assertEquals("fixture-B-access", manager.snapshot().accessToken)
        }
    }
}
