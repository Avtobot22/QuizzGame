package com.quizzgame.app.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.app.startup.GraphAccessResult
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.core.session.lifecycle.SessionOperation
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageFailure
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppRootViewModelTest {
    @Test
    fun externalRequestSurvivesRecreationAndAnOldAcknowledgementCannotClearANewerRequest() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val savedState = SavedStateHandle()
            val startup = LogoutTestStartup()
            val first = AppRootViewModel(startup, savedState)
            store.put("first", first)
            val request = NavigationRequest("first", NavigationKey, NavigationKey)
            first.requestNavigation(request)
            val restoredState = SavedStateHandle(
                mapOf("navigation-request" to savedState.get<String>("navigation-request")),
            )
            val restored = AppRootViewModel(startup, restoredState)
            store.put("restored", restored)
            assertEquals(request, restored.pendingNavigation.value)
            val newer = request.copy(id = "newer")
            restored.requestNavigation(newer)
            restored.navigationRequestHandled(request)
            assertEquals(newer, restored.pendingNavigation.value)
            restored.navigationRequestHandled(newer)
            assertEquals(null, restored.pendingNavigation.value)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun doubleTapSharesLogoutAndEveryAppliedLogoutAdvancesRevisionEvenForGuest() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val startup = LogoutTestStartup()
            val viewModel = AppRootViewModel(startup, SavedStateHandle())
            store.put("root", viewModel)
            val initialEntry = viewModel.entryState.value as AppEntryState.Ready
            viewModel.logout()
            viewModel.logout()
            runCurrent()
            assertEquals(1, startup.logouts)
            assertEquals(0L, viewModel.logoutRevision.value)
            startup.gate.complete(Unit)
            runCurrent()
            assertEquals(1L, viewModel.logoutRevision.value)
            assertFalse(viewModel.isCurrentEntry(initialEntry))
            assertNotEquals(initialEntry, viewModel.entryState.value)
            viewModel.logout()
            runCurrent()
            assertEquals(2, startup.logouts)
            assertEquals(2L, viewModel.logoutRevision.value)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun staleEntryIsRejectedImmediatelyBeforeThePublishedEntryFlowCatchesUp() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val startup = LogoutTestStartup()
            val viewModel = AppRootViewModel(startup, SavedStateHandle())
            store.put("root", viewModel)
            val initial = viewModel.entryState.value as AppEntryState.Ready
            assertTrue(viewModel.isCurrentEntry(initial))
            startup.sessionState.value = SessionState.Expired
            assertFalse(viewModel.isCurrentEntry(initial))
            runCurrent()
            assertEquals(AppEntryScreen.SIGN_IN, (viewModel.entryState.value as AppEntryState.Ready).screen)
            startup.sessionState.value = SessionState.Unavailable(SessionStorageFailure.WRITE)
            runCurrent()
            assertTrue(viewModel.entryState.value is AppEntryState.Blocked)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedLogoutDoesNotAdvanceNavigationRevision() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val startup = LogoutTestStartup().apply {
                result = SessionOperation.Failed(SessionStorageFailure.WRITE)
                gate.complete(Unit)
            }
            val viewModel = AppRootViewModel(startup, SavedStateHandle())
            store.put("root", viewModel)
            viewModel.logout()
            runCurrent()
            assertEquals(1, startup.logouts)
            assertEquals(0L, viewModel.logoutRevision.value)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun logoutRevisionSurvivesSavedStateRecreation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val startup = LogoutTestStartup().apply { gate.complete(Unit) }
            val handle = SavedStateHandle()
            val first = AppRootViewModel(startup, handle)
            store.put("first", first)
            first.logout()
            runCurrent()
            val restored = AppRootViewModel(
                startup,
                SavedStateHandle(mapOf("logout-revision" to handle.get<Long>("logout-revision"))),
            )
            store.put("restored", restored)
            assertEquals(first.logoutRevision.value, restored.logoutRevision.value)
            assertEquals(
                (first.entryState.value as AppEntryState.Ready).restorationOwner,
                (restored.entryState.value as AppEntryState.Ready).restorationOwner,
            )
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Serializable
    data object NavigationKey : NavKey
}

private class LogoutTestStartup : AppStartup {
    override val state = MutableStateFlow<AppStartupState>(AppStartupState.Ready)
    override val sessionState = MutableStateFlow<SessionState>(SessionState.Guest)
    val gate = CompletableDeferred<Unit>()
    var result: SessionOperation = SessionOperation.Applied
    var logouts = 0

    override suspend fun ensureStarted() = Unit
    override suspend fun retryInitialization() = Unit
    override suspend fun accessGraph(): GraphAccessResult = error("Logout must not request an application graph")
    override suspend fun logout(): SessionOperation {
        logouts++
        gate.await()
        return result
    }
}
