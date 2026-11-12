package com.quizzgame.feature.splash.mobile.presentation

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.AuthenticationKind
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.shared.splash.domain.SplashRepository
import com.quizzgame.shared.splash.domain.StartSplashUseCase
import com.quizzgame.shared.splash.domain.entity.SplashOutcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MobileSplashViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }

    @After fun cleanup() { Dispatchers.resetMain() }

    @Test
    fun startupOutcomeSelectsTheConcreteDestinationBeforeUiHandlesNavigation() = runTest(dispatcher) {
        val destinations = mapOf(
            SplashOutcome.Continue to MobileSplashDestination.JOIN_ROOM,
            SplashOutcome.SignIn to MobileSplashDestination.SIGN_IN,
        )
        destinations.forEach { (outcome, destination) ->
            val vm = createViewModel { outcome }
            advanceUntilIdle()
            assertEquals(destination, (vm.state.value as MobileSplashUiState.NavigationPending).destination)
            runCurrent()
            assertEquals(destination, (vm.state.value as MobileSplashUiState.NavigationPending).destination)
        }
    }

    @Test
    fun signInActionChoosesItsDestinationAndMatchingAcknowledgementConsumesIt() = runTest(dispatcher) {
        val vm = createViewModel {
            SplashOutcome.Failed(AppFailure.Authentication(AuthenticationKind.REQUIRED))
        }
        advanceUntilIdle()
        vm.onIntent(
            MobileSplashIntent.NavigationHandled(
                (vm.state.value as? MobileSplashUiState.NavigationPending)?.requestId ?: "stale",
            ),
        )
        assertTrue(vm.state.value is MobileSplashUiState.Failed)
        vm.onIntent(MobileSplashIntent.SignIn)
        assertEquals(
            MobileSplashDestination.SIGN_IN,
            (vm.state.value as MobileSplashUiState.NavigationPending).destination,
        )
        vm.onIntent(
            MobileSplashIntent.NavigationHandled(
                (vm.state.value as? MobileSplashUiState.NavigationPending)?.requestId ?: "stale",
            ),
        )
        assertEquals(MobileSplashUiState.NavigationComplete, vm.state.value)
    }

    @Test
    fun repeatedRetryWhileLoadingDoesNotStartConcurrentAttempts() = runTest(dispatcher) {
        var calls = 0
        val pending = CompletableDeferred<SplashOutcome>()
        val vm = createViewModel {
            calls++
            pending.await()
        }
        vm.onIntent(MobileSplashIntent.Retry)
        runCurrent()
        vm.onIntent(MobileSplashIntent.Retry)
        assertEquals(1, calls)
        pending.complete(SplashOutcome.Continue)
        advanceUntilIdle()
        assertTrue(vm.state.value is MobileSplashUiState.NavigationPending)
    }

    @Test
    fun retryAfterFailureRunsWholeAttemptOnceAndAcknowledgementConsumesNavigation() = runTest(dispatcher) {
        var calls = 0
        val vm = createViewModel {
            calls++
            if (calls == 1) {
                SplashOutcome.Failed(AppFailure.Connectivity(ConnectivityKind.TIMEOUT))
            } else {
                SplashOutcome.Continue
            }
        }
        advanceUntilIdle()
        assertTrue(vm.state.value is MobileSplashUiState.Failed)
        vm.onIntent(MobileSplashIntent.Retry)
        vm.onIntent(MobileSplashIntent.Retry)
        advanceUntilIdle()
        assertEquals(2, calls)
        assertTrue(vm.state.value is MobileSplashUiState.NavigationPending)
        vm.onIntent(
            MobileSplashIntent.NavigationHandled(
                (vm.state.value as? MobileSplashUiState.NavigationPending)?.requestId ?: "stale",
            ),
        )
        assertEquals(MobileSplashUiState.NavigationComplete, vm.state.value)
    }

    @Test
    fun interruptedAttemptShowsAnErrorAndCanBeRetried() = runTest(dispatcher) {
        var calls = 0
        val vm = createViewModel {
            calls++
            if (calls == 1) SplashOutcome.Failed(AppFailure.OperationInterrupted) else SplashOutcome.Continue
        }
        advanceUntilIdle()
        val failed = vm.state.value as MobileSplashUiState.Failed
        assertTrue(ErrorActionKind.RETRY in failed.spec.actions)
        vm.onIntent(MobileSplashIntent.Retry)
        advanceUntilIdle()
        assertEquals(2, calls)
        assertTrue(vm.state.value is MobileSplashUiState.NavigationPending)
    }

    @Test
    fun configurationAndStorageFailuresDoNotOfferRetry() = runTest(dispatcher) {
        listOf(AppFailure.Configuration, AppFailure.Storage(StorageKind.READ)).forEach { failure ->
            val vm = createViewModel { SplashOutcome.Failed(failure) }
            advanceUntilIdle()
            val failed = vm.state.value as MobileSplashUiState.Failed
            assertTrue(ErrorActionKind.RETRY !in failed.spec.actions)
        }
    }

    @Test
    fun staleAcknowledgementDoesNotConsumeANewResultAndNoCollectorIsRequired() = runTest(dispatcher) {
        val vm = createViewModel { SplashOutcome.Continue }
        advanceUntilIdle()
        val pending = vm.state.value as MobileSplashUiState.NavigationPending
        vm.onIntent(MobileSplashIntent.NavigationHandled("previous-attempt"))
        assertEquals(pending, vm.state.value)
        vm.onIntent(MobileSplashIntent.NavigationHandled(pending.requestId))
        vm.onIntent(MobileSplashIntent.NavigationHandled(pending.requestId))
        assertEquals(MobileSplashUiState.NavigationComplete, vm.state.value)
    }

    private fun createViewModel(outcome: suspend () -> SplashOutcome): MobileSplashViewModel {
        var result: SplashOutcome = SplashOutcome.Continue
        val repository = object : SplashRepository {
            override suspend fun currentSession(): SessionState = SessionState.Guest

            override suspend fun loadLaunch(): AppFailure? {
                result = outcome()
                return (result as? SplashOutcome.Failed)?.failure
            }

            override suspend fun refreshFlags(): AppFailure? = null
            override suspend fun loadUser(expectedGeneration: Long): AppFailure? =
                error("Guest fixture must not load user")
            override fun guestAllowed(): Boolean = result == SplashOutcome.Continue
            override fun clearUser() = Unit
        }
        return MobileSplashViewModel(StartSplashUseCase(repository))
    }
}
