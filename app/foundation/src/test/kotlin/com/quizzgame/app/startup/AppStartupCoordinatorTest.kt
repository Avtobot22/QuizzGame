package com.quizzgame.app.startup

import com.quizzgame.app.network.AppNetworkConfigurationException
import com.quizzgame.app.network.AppNetworkGraphFactory
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.session.lifecycle.SessionManager
import com.quizzgame.core.session.lifecycle.SessionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AppStartupCoordinatorTest {
    @Test
    fun concurrentCallersShareOneFlightAndCancellingAScreenDoesNotCancelStartup() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val gate = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()
        val store = StartupTestStore(events)
        val manager = SessionManager.open(store, dispatcher)
        var opens = 0
        val coordinator = AppStartupCoordinator(
            openSession = {
                opens++
                gate.await()
                manager
            },
            networkFactory = fakeNetworkFactory { events += "network-close" },
            dispatcher = dispatcher,
        )
        val first = launch { coordinator.ensureStarted() }
        val second = launch { coordinator.ensureStarted() }
        runCurrent()
        assertEquals(AppStartupState.Loading, coordinator.state.value)
        assertEquals(1, opens)
        first.cancel()
        gate.complete(Unit)
        second.join()
        assertEquals(AppStartupState.Ready, coordinator.state.value)
        assertSame(manager, (coordinator.accessGraph() as GraphAccessResult.Ready).graph.session)
        coordinator.ensureStarted()
        coordinator.retryInitialization()
        assertEquals(1, opens)
        coordinator.shutdown()
        coordinator.shutdown()
        assertEquals(listOf("network-close", "session-close"), events)
        assertEquals(SessionState.Closed, manager.state.value)
    }

    @Test
    fun unavailableStorageRetriesTheSameManagerWithoutCreatingNetworkEarly() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = StartupTestStore().apply { failRead = true }
        var opens = 0
        var networks = 0
        val coordinator = AppStartupCoordinator(
            openSession = {
                opens++
                SessionManager.open(store, dispatcher)
            },
            networkFactory = AppNetworkGraphFactory { session, json ->
                networks++
                fakeNetworkFactory().open(session, json)
            },
            dispatcher = dispatcher,
        )
        coordinator.ensureStarted()
        assertTrue(coordinator.state.value is AppStartupState.Failed)
        assertEquals(0, networks)
        coordinator.ensureStarted()
        assertEquals(1, store.reads)
        store.failRead = false
        coordinator.retryInitialization()
        assertEquals(AppStartupState.Ready, coordinator.state.value)
        assertEquals(
            SessionState.Guest,
            (coordinator.accessGraph() as GraphAccessResult.Ready).graph.session.state.value,
        )
        assertEquals(1, opens)
        assertEquals(2, store.reads)
        assertEquals(1, networks)
        coordinator.shutdown()
    }

    @Test
    fun networkStartupFailureDoesNotPublishReadyAndCanBeRetried() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val store = StartupTestStore()
        var attempts = 0
        val coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(store, dispatcher) },
            networkFactory = AppNetworkGraphFactory { session, json ->
                attempts++
                if (attempts == 1) throw IOException("Local mock transport startup failed")
                fakeNetworkFactory().open(session, json)
            },
            dispatcher = dispatcher,
        )
        coordinator.ensureStarted()
        val failed = coordinator.state.value as AppStartupState.Failed
        assertEquals(AppStartupFailure.Transport, failed.reason)
        assertEquals(ErrorCopyKey.GENERIC_FAILURE, failed.error.title)
        assertEquals(1, store.reads)
        coordinator.retryInitialization()
        assertEquals(AppStartupState.Ready, coordinator.state.value)
        assertEquals(2, attempts)
        assertEquals(1, store.reads)
        coordinator.shutdown()
    }

    @Test
    fun missingRealConfigurationIsExplicitAndDoesNotPretendToBeOffline() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(StartupTestStore(), dispatcher) },
            networkFactory = AppNetworkGraphFactory { _, _ -> throw AppNetworkConfigurationException() },
            dispatcher = dispatcher,
        )
        coordinator.ensureStarted()
        val failed = coordinator.state.value as AppStartupState.Failed
        assertTrue(failed.configurationRequired)
        assertTrue(failed.error.actions.isEmpty())
        coordinator.shutdown()
    }

    @Test
    fun unexpectedProgrammingFailurePropagatesAndShutdownStillClosesStorage() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val events = mutableListOf<String>()
        val coordinator = AppStartupCoordinator(
            openSession = { SessionManager.open(StartupTestStore(events), dispatcher) },
            networkFactory = AppNetworkGraphFactory { _, _ -> error("Broken graph implementation") },
            dispatcher = dispatcher,
        )
        var failure: IllegalStateException? = null
        try {
            coordinator.ensureStarted()
        } catch (expected: IllegalStateException) {
            failure = expected
        }
        assertEquals("Broken graph implementation", failure?.message)
        coordinator.shutdown()
        assertEquals(listOf("session-close"), events)
    }

    @Test
    fun initializerCancellationIsNotConvertedIntoAFailedOrGuestState() = runTest {
        val coordinator = AppStartupCoordinator(
            openSession = { throw CancellationException("Application scope stopped") },
            networkFactory = fakeNetworkFactory(),
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        var cancelled = false
        try {
            coordinator.ensureStarted()
        } catch (_: CancellationException) {
            cancelled = true
        }
        assertTrue(cancelled)
        assertEquals(SessionState.Initializing, coordinator.sessionState.value)
        assertTrue(coordinator.state.value !is AppStartupState.Failed)
        coordinator.shutdown()
    }
}
