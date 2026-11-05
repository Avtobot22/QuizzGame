package com.quizzgame.app.startup

import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageException
import com.quizzgame.core.session.storage.SessionStorageFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AppStartupAvailabilityTest {
    @Test
    fun failedAccessIsTypedAndDoesNotAutomaticallyOpenStorageAgain() = runTest {
        var attempts = 0
        val coordinator = AppStartupCoordinator(
            openSession = {
                attempts++
                throw IOException("Local file cannot be opened")
            },
            networkFactory = fakeNetworkFactory(),
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        val result = coordinator.accessGraph() as GraphAccessResult.Unavailable
        assertEquals(AppStartupFailure.Storage(SessionStorageFailure.READ), result.failure.reason)
        assertEquals(ErrorCopyKey.STORAGE_PROBLEM, result.failure.error.title)
        assertEquals(listOf(ErrorActionKind.RETRY), result.failure.error.actions)
        assertEquals(result, coordinator.accessGraph())
        coordinator.ensureStarted()
        assertEquals(1, attempts)
        coordinator.shutdown()
    }

    @Test
    fun openingCryptoFailureIsPreservedAndDoesNotOfferRetryOrSignIn() = runTest {
        var attempts = 0
        val coordinator = AppStartupCoordinator(
            openSession = {
                attempts++
                throw SessionStorageException(SessionStorageFailure.CRYPTO)
            },
            networkFactory = fakeNetworkFactory(),
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        val result = coordinator.accessGraph() as GraphAccessResult.Unavailable
        assertEquals(AppStartupFailure.Storage(SessionStorageFailure.CRYPTO), result.failure.reason)
        assertTrue(result.failure.error.actions.isEmpty())
        coordinator.retryInitialization()
        assertEquals(1, attempts)
        coordinator.shutdown()
    }

    @Test
    fun readyAccessReusesItsGraphAndShutdownIsTerminalAndIdempotent() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = StartupTestStore()
        var networks = 0
        var closes = 0
        val coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(store, dispatcher) },
            networkFactory = AppNetworkGraphFactory { session, json ->
                networks++
                fakeNetworkFactory { closes++ }.open(session, json)
            },
            dispatcher = dispatcher,
        )
        val first = coordinator.accessGraph() as GraphAccessResult.Ready
        val second = coordinator.accessGraph() as GraphAccessResult.Ready
        assertSame(first.graph, second.graph)
        val closeA = launch { coordinator.shutdown() }
        val closeB = launch { coordinator.shutdown() }
        closeA.join()
        closeB.join()
        coordinator.ensureStarted()
        coordinator.retryInitialization()
        assertEquals(GraphAccessResult.Closed, coordinator.accessGraph())
        assertEquals(AppStartupState.Closed, coordinator.state.value)
        assertEquals(SessionState.Closed, coordinator.sessionState.value)
        assertEquals(1, networks)
        assertEquals(1, closes)
        assertEquals(1, store.closes)
    }

    @Test
    fun shutdownReleasesSharedWaitersAsClosedWithoutInventingAStartupFailure() = runTest {
        val entered = CompletableDeferred<Unit>()
        val coordinator = AppStartupCoordinator(
            openSession = {
                entered.complete(Unit)
                awaitCancellation()
            },
            networkFactory = fakeNetworkFactory(),
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        val access = async { coordinator.accessGraph() }
        entered.await()
        coordinator.shutdown()
        assertEquals(GraphAccessResult.Closed, access.await())
        assertEquals(GraphAccessResult.Closed, coordinator.accessGraph())
    }

    @Test
    fun cancellingAnAccessCallerDoesNotCancelStartupOrBecomeClosed() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val gate = CompletableDeferred<Unit>()
        val coordinator = AppStartupCoordinator(
            openSession = {
                gate.await()
                SessionManager.open(StartupTestStore(), dispatcher)
            },
            networkFactory = fakeNetworkFactory(),
            dispatcher = dispatcher,
        )
        var cancelled = false
        val caller = launch {
            try {
                coordinator.accessGraph()
            } catch (_: CancellationException) {
                cancelled = true
            }
        }
        runCurrent()
        caller.cancel()
        caller.join()
        assertTrue(cancelled)
        gate.complete(Unit)
        assertTrue(coordinator.accessGraph() is GraphAccessResult.Ready)
        coordinator.shutdown()
    }

    @Test
    fun shutdownDuringNetworkCreationClosesLateResourcesExactlyOnce() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = StartupTestStore()
        var graphCloses = 0
        lateinit var coordinator: AppStartupCoordinator
        lateinit var closing: Job
        coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(store, dispatcher) },
            networkFactory = AppNetworkGraphFactory { session, json ->
                closing = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { coordinator.shutdown() }
                fakeNetworkFactory { graphCloses++ }.open(session, json)
            },
            dispatcher = dispatcher,
        )
        val access = async { coordinator.accessGraph() }
        runCurrent()
        closing.join()
        assertEquals(GraphAccessResult.Closed, access.await())
        assertEquals(1, graphCloses)
        assertEquals(1, store.closes)
        coordinator.shutdown()
        assertEquals(1, graphCloses)
        assertEquals(1, store.closes)
    }
}
