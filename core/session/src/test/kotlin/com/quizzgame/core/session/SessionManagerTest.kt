package com.quizzgame.core.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SessionManagerTest {

    @Test
    fun `restored credentials are ready and reading snapshots never loads storage`() = runTest {
        val store = FakeSessionStore(TokenPair("access-1", "refresh-1"))
        val manager = SessionManager.open(store)
        repeat(20) {
            assertEquals("access-1", manager.snapshot().accessToken)
            assertEquals(TEST_DEVICE_ID, manager.deviceIdProvider.get())
        }
        assertEquals(1, store.reads)
        assertEquals(SessionState.Authenticated(1), manager.state.value)
        assertEquals(1L, manager.snapshot().sessionGeneration)
    }

    @Test
    fun `login logout and new login advance generation and keep device identity`() = runTest {
        val store = FakeSessionStore()
        val manager = SessionManager.open(store)
        val device = manager.deviceIdProvider
        assertEquals(SessionState.Guest, manager.state.value)
        assertEquals(SessionOperation.Applied, manager.startSession(TokenPair("access-1", "refresh-1")))
        val first = manager.snapshot()
        assertEquals("refresh-1", store.tokens!!.refresh)
        assertEquals(SessionOperation.Applied, manager.logout())
        assertNull(store.tokens)
        assertNull(manager.snapshot().accessToken)
        assertTrue(manager.snapshot().sessionGeneration > first.sessionGeneration)
        assertEquals(SessionState.Guest, manager.state.value)
        assertSame(device, manager.deviceIdProvider)
        assertEquals(TEST_DEVICE_ID, manager.deviceIdProvider.get())
        manager.startSession(TokenPair("access-2", "refresh-2"))
        assertEquals("access-2", manager.snapshot().accessToken)
        assertEquals("refresh-2", store.tokens!!.refresh)
    }

    @Test
    fun `initial read failure is unavailable rather than a guest session and can be retried`() = runTest {
        val store = FakeSessionStore(TokenPair("saved-access", "saved-refresh")).apply { failRead = true }
        val manager = SessionManager.open(store)
        assertEquals(SessionState.Unavailable(SessionStorageFailure.READ), manager.state.value)
        assertThrows(SessionUnavailableException::class.java) { manager.snapshot() }
        assertThrows(SessionUnavailableException::class.java) { manager.deviceIdProvider.get() }
        assertEquals("saved-access", store.tokens!!.access)
        assertEquals(0, store.writes)
        store.failRead = false
        assertEquals(SessionOperation.Applied, manager.restoreStoredSession())
        assertEquals("saved-access", manager.snapshot().accessToken)
    }

    @Test
    fun `failed login write preserves the full old pair and ready generation`() = runTest {
        val original = TokenPair("saved-access", "saved-refresh")
        val store = FakeSessionStore(original)
        val manager = SessionManager.open(store)
        val before = manager.snapshot()
        store.failWrite = true
        assertEquals(
            SessionOperation.Failed(SessionStorageFailure.WRITE),
            manager.startSession(TokenPair("new-access", "new-refresh")),
        )
        assertSame(original, store.tokens)
        assertEquals(before.accessToken, manager.snapshot().accessToken)
        assertEquals(before.sessionGeneration, manager.snapshot().sessionGeneration)
        assertEquals(
            SessionState.Unavailable(SessionStorageFailure.WRITE, SessionState.Authenticated(before.sessionGeneration)),
            manager.state.value,
        )
    }

    @Test
    fun `failed logout does not claim successful clearing and can be retried`() = runTest {
        val store = FakeSessionStore(TokenPair("saved-access", "saved-refresh"))
        val manager = SessionManager.open(store)
        store.failWrite = true
        assertEquals(SessionOperation.Failed(SessionStorageFailure.WRITE), manager.logout())
        assertEquals("saved-access", store.tokens!!.access)
        assertEquals("saved-access", manager.snapshot().accessToken)
        store.failWrite = false
        assertEquals(SessionOperation.Applied, manager.logout())
        assertNull(store.tokens)
        assertEquals(SessionState.Guest, manager.state.value)
    }

    @Test
    fun `refresh replaces both tokens without changing session identity and rejects older access`() = runTest {
        val store = FakeSessionStore(TokenPair("access-1", "refresh-1"))
        val manager = SessionManager.open(store)
        val observed = manager.snapshot()
        assertEquals(
            SessionOperation.Applied,
            manager.applyRefreshedTokensIfCurrentBlocking(observed, TokenPair("access-2", "refresh-2")),
        )
        assertEquals(observed.sessionGeneration, manager.snapshot().sessionGeneration)
        assertEquals("access-2", manager.snapshot().accessToken)
        assertEquals("refresh-2", store.tokens!!.refresh)
        assertEquals(
            SessionOperation.Stale,
            manager.applyRefreshedTokensIfCurrentBlocking(observed, TokenPair("stale-access", "stale-refresh")),
        )
        assertEquals("access-2", manager.snapshot().accessToken)
    }

    @Test
    fun `only explicit current session revocation enters Expired`() = runTest {
        val store = FakeSessionStore(TokenPair("access-1", "refresh-1"))
        val manager = SessionManager.open(store)
        assertEquals(SessionOperation.Applied, manager.expireIfCurrentBlocking(manager.snapshot()))
        assertEquals(SessionState.Expired, manager.state.value)
        assertNull(manager.snapshot().accessToken)
        assertNull(store.tokens)
    }

    @Test
    fun `delayed refresh cannot resurrect a logout or overwrite another login`() = runTest {
        listOf(false, true).forEach { replaceLogin ->
            val store = FakeSessionStore(TokenPair("access-1", "refresh-1"))
            val manager = SessionManager.open(store)
            val observed = manager.snapshot()
            val entered = CountDownLatch(1)
            val released = CountDownLatch(1)
            val executor = Executors.newSingleThreadExecutor()
            try {
                val refresh = executor.submit<SessionOperation> {
                    entered.countDown()
                    check(released.await(5, TimeUnit.SECONDS))
                    manager.applyRefreshedTokensIfCurrentBlocking(observed, TokenPair("late-access", "late-refresh"))
                }
                assertTrue(entered.await(5, TimeUnit.SECONDS))
                if (replaceLogin) manager.startSession(TokenPair("other-access", "other-refresh")) else manager.logout()
                released.countDown()
                assertEquals(SessionOperation.Stale, refresh.get(5, TimeUnit.SECONDS))
                assertEquals(if (replaceLogin) "other-access" else null, manager.snapshot().accessToken)
                assertEquals(if (replaceLogin) "other-refresh" else null, store.tokens?.refresh)
            } finally {
                released.countDown()
                executor.shutdownNow()
            }
        }
    }

    @Test
    fun `cancellation during blocking commit cannot separate disk and in memory state`() = runTest {
        val entered = CountDownLatch(1)
        val released = CountDownLatch(1)
        val store = object : SessionStore {
            var record = SessionRecord(TEST_DEVICE_ID, null)
            val tokens get() = record.tokens
            override fun read(): SessionRecord = record
            override fun write(tokens: TokenPair?): SessionRecord {
                entered.countDown()
                check(released.await(5, TimeUnit.SECONDS))
                record = SessionRecord(record.deviceId, tokens)
                return record
            }
        }
        val manager = SessionManager.open(store)
        val job = launch(Dispatchers.IO) { manager.startSession(TokenPair("committed-access", "committed-refresh")) }
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            job.cancel()
        } finally {
            released.countDown()
        }
        job.join()
        assertTrue(job.isCancelled)
        assertEquals(store.tokens!!.access, manager.snapshot().accessToken)
        assertEquals("committed-refresh", store.tokens!!.refresh)
        assertTrue(manager.state.value is SessionState.Authenticated)
    }

    @Test
    fun `token validation and public diagnostics do not disclose credentials`() {
        val pair = TokenPair("fixture-sensitive-access", "fixture-sensitive-refresh")
        assertFalse(pair.toString().contains("fixture-sensitive"))
        assertThrows(IllegalArgumentException::class.java) { TokenPair("access", " ") }
        val failure = assertThrows(IllegalArgumentException::class.java) { TokenPair("fixture-sensitive\n", "refresh") }
        assertFalse(failure.toString().contains("fixture-sensitive"))
        assertNull(SessionStorageException(SessionStorageFailure.CORRUPT).cause)
        val record = SessionRecord(TEST_DEVICE_ID, pair)
        assertFalse(record.toString().contains(TEST_DEVICE_ID))
        assertFalse(record.toString().contains("fixture-sensitive"))
    }

    @Test
    fun `read failure retains the last authenticated state and credentials for retry`() = runTest {
        val store = FakeSessionStore(TokenPair("saved-access", "saved-refresh"))
        val manager = SessionManager.open(store)
        val before = manager.snapshot()
        store.failRead = true
        assertEquals(SessionOperation.Failed(SessionStorageFailure.READ), manager.restoreStoredSession())
        assertEquals(
            SessionState.Unavailable(SessionStorageFailure.READ, SessionState.Authenticated(before.sessionGeneration)),
            manager.state.value,
        )
        assertEquals(before.accessToken, manager.snapshot().accessToken)
        assertEquals(before.sessionGeneration, manager.snapshot().sessionGeneration)
    }

    @Test
    fun `explicit restoration advances generation and rejects results from before restoration`() = runTest {
        val store = FakeSessionStore(TokenPair("saved-access", "saved-refresh"))
        val manager = SessionManager.open(store)
        val before = manager.snapshot()
        assertEquals(SessionOperation.Applied, manager.restoreStoredSession())
        assertTrue(manager.snapshot().sessionGeneration > before.sessionGeneration)
        assertEquals(
            SessionOperation.Stale,
            manager.applyRefreshedTokensIfCurrentBlocking(before, TokenPair("late-access", "late-refresh")),
        )
        assertEquals("saved-access", manager.snapshot().accessToken)
    }

    @Test
    fun `close releases its store once and rejects further operations without deleting tokens`() = runTest {
        val store = FakeSessionStore(TokenPair("saved-access", "saved-refresh"))
        val manager = SessionManager.open(store)
        val before = manager.snapshot()
        manager.close()
        manager.close()
        assertEquals(1, store.closes)
        assertEquals(SessionState.Closed, manager.state.value)
        assertThrows(SessionUnavailableException::class.java) { manager.snapshot() }
        assertThrows(SessionUnavailableException::class.java) { manager.deviceIdProvider.get() }
        assertEquals(SessionOperation.Closed, manager.logout())
        assertEquals(SessionOperation.Closed, manager.restoreStoredSession())
        assertEquals(SessionOperation.Closed, manager.startSession(TokenPair("new-access", "new-refresh")))
        assertEquals(SessionOperation.Closed, manager.expireIfCurrentBlocking(before))
        assertEquals(1, store.reads)
        assertEquals(0, store.writes)
        assertEquals("saved-refresh", store.tokens!!.refresh)
    }

    @Test
    fun `cancelled initialization closes the acquired store`() = runTest {
        val entered = CountDownLatch(1)
        val released = CountDownLatch(1)
        val store = object : SessionStore {
            var closes = 0
            override fun read(): SessionRecord {
                entered.countDown()
                check(released.await(5, TimeUnit.SECONDS))
                return SessionRecord(TEST_DEVICE_ID, null)
            }

            override fun write(tokens: TokenPair?) = SessionRecord(TEST_DEVICE_ID, tokens)
            override fun close() {
                closes++
            }
        }
        val job = launch(Dispatchers.IO) { SessionManager.open(store) }
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            job.cancel()
        } finally {
            released.countDown()
        }
        job.join()
        assertTrue(job.isCancelled)
        assertEquals(1, store.closes)
    }
}

internal const val TEST_DEVICE_ID = "e4a508be-bb66-46d3-91f6-7b4cabfc977b"

internal class FakeSessionStore(tokens: TokenPair? = null) : SessionStore {

    var record = SessionRecord(TEST_DEVICE_ID, tokens)
    val tokens get() = record.tokens
    var failRead = false
    var failWrite = false
    var reads = 0
    var writes = 0
    var closes = 0

    override fun close() {
        closes++
    }

    override fun read(): SessionRecord {
        reads++
        if (failRead) throw IOException("fixture failure")
        return record
    }

    override fun write(tokens: TokenPair?): SessionRecord {
        writes++
        if (failWrite) throw IOException("fixture failure")
        record = SessionRecord(record.deviceId, tokens)
        return record
    }
}
