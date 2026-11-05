package com.quizzgame.app.startup

import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.lifecycle.SessionOperation
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionStorageFailure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppStartupRecoveryTest {
    @Test
    fun runtimeReadRecoveryIsSingleFlightAndReusesTheGraphAndSession() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val tokens = TokenPair("fixture-access", "fixture-refresh")
        val store = StartupTestStore(initialTokens = tokens)
        var networks = 0
        val coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(store, dispatcher) },
            networkFactory = AppNetworkGraphFactory { session, json ->
                networks++
                fakeNetworkFactory().open(session, json)
            },
            dispatcher = dispatcher,
        )
        val ready = coordinator.accessGraph() as GraphAccessResult.Ready
        store.failRead = true
        ready.graph.session.restoreStoredSession()
        runCurrent()
        assertSame(ready.graph, (coordinator.accessGraph() as GraphAccessResult.Ready).graph)
        val failure = coordinator.state.value as AppStartupState.Failed
        assertEquals(AppStartupFailure.Storage(SessionStorageFailure.READ), failure.reason)
        assertEquals(listOf(ErrorActionKind.RETRY), failure.error.actions)
        store.failRead = false
        listOf(async { coordinator.retryInitialization() }, async { coordinator.retryInitialization() }).awaitAll()
        val restored = coordinator.accessGraph() as GraphAccessResult.Ready
        assertSame(ready.graph, restored.graph)
        assertSame(ready.graph.session, restored.graph.session)
        assertTrue(restored.graph.session.state.value is SessionState.Authenticated)
        assertEquals(3, store.reads)
        assertEquals(0, store.writes)
        assertEquals(tokens, store.tokens)
        assertEquals(1, networks)
        coordinator.shutdown()
    }

    @Test
    fun failedReadRetryStaysUnavailableAndNeverWritesOrSignsOut() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = StartupTestStore().apply { failRead = true }
        val coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(store, dispatcher) },
            networkFactory = AppNetworkGraphFactory { _, _ -> error("Unavailable storage must block network startup") },
            dispatcher = dispatcher,
        )
        coordinator.ensureStarted()
        coordinator.retryInitialization()
        val failure = coordinator.accessGraph() as GraphAccessResult.Unavailable
        assertEquals(AppStartupFailure.Storage(SessionStorageFailure.READ), failure.failure.reason)
        assertEquals(2, store.reads)
        assertEquals(0, store.writes)
        coordinator.shutdown()
    }

    @Test
    fun permanentRuntimeStorageFailuresBlockReadRetryLogoutAndGraphAccess() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val tokens = TokenPair("fixture-access", "fixture-refresh")
        val permanentFailures = listOf(
            SessionStorageFailure.CRYPTO,
            SessionStorageFailure.CORRUPT,
        )
        permanentFailures.forEach { reason ->
            val store = StartupTestStore(initialTokens = tokens)
            val coordinator = AppStartupCoordinator(
                openSession = { SessionManager.open(store, dispatcher) },
                networkFactory = fakeNetworkFactory(),
                dispatcher = dispatcher,
            )
            val graph = (coordinator.accessGraph() as GraphAccessResult.Ready).graph
            store.readFailure = reason
            graph.session.restoreStoredSession()
            runCurrent()
            assertTrue(coordinator.accessGraph() is GraphAccessResult.Ready)
            val failure = coordinator.state.value as AppStartupState.Failed
            assertEquals(AppStartupFailure.Storage(reason), failure.reason)
            assertTrue(failure.error.actions.isEmpty())
            coordinator.retryInitialization()
            assertEquals(SessionOperation.Failed(reason), coordinator.logout())
            assertEquals(2, store.reads)
            assertEquals(0, store.writes)
            assertEquals(tokens, store.tokens)
            coordinator.shutdown()
        }
    }

    @Test
    fun concurrentLogoutsShareOneDurableClearAndGuestCanExplicitlyLogoutAgain() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = StartupTestStore(initialTokens = TokenPair("fixture-access", "fixture-refresh"))
        val coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(store, dispatcher) },
            networkFactory = fakeNetworkFactory(),
            dispatcher = dispatcher,
        )
        coordinator.ensureStarted()
        assertEquals(
            listOf(SessionOperation.Applied, SessionOperation.Applied),
            listOf(async { coordinator.logout() }, async { coordinator.logout() }).awaitAll(),
        )
        assertEquals(2, store.writes)
        assertEquals(SessionState.Guest, coordinator.sessionState.value)
        assertEquals(SessionOperation.Applied, coordinator.logout())
        assertEquals(4, store.writes)
        coordinator.shutdown()
        assertEquals(SessionOperation.Closed, coordinator.logout())
    }

    @Test
    fun failedLogoutRetriesOnlyItsPendingLocalClear() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val tokens = TokenPair("fixture-access", "fixture-refresh")
        val store = StartupTestStore(initialTokens = tokens)
        val coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(store, dispatcher) },
            networkFactory = fakeNetworkFactory(),
            dispatcher = dispatcher,
        )
        coordinator.ensureStarted()
        store.writeFailure = SessionStorageFailure.WRITE
        assertEquals(SessionOperation.Failed(SessionStorageFailure.WRITE), coordinator.logout())
        runCurrent()
        val failure = coordinator.state.value as AppStartupState.Failed
        assertEquals(AppStartupFailure.Storage(SessionStorageFailure.WRITE), failure.reason)
        assertEquals(listOf(ErrorActionKind.RETRY), failure.error.actions)
        assertEquals(tokens, store.tokens)
        store.writeFailure = null
        coordinator.retryInitialization()
        assertEquals(SessionState.Guest, coordinator.sessionState.value)
        assertEquals(2, store.reads)
        coordinator.shutdown()
    }
}
