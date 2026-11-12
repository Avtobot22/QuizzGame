package com.quizzgame.feature.splash.tv.presentation

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
class TvSplashViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }

    @After fun cleanup() { Dispatchers.resetMain() }

    @Test
    fun startupOutcomeSelectsTheConcreteDestinationBeforeUiHandlesNavigation() = runTest(dispatcher) {
        val destinations = mapOf(
            SplashOutcome.Continue to TvSplashDestination.DASHBOARD,
            SplashOutcome.SignIn to TvSplashDestination.SIGN_IN,
        )
        destinations.forEach { (outcome, destination) ->
            val vm = createViewModel { outcome }
            advanceUntilIdle()
            assertEquals(destination, (vm.state.value as TvSplashUiState.NavigationPending).destination)
            runCurrent()
            assertEquals(destination, (vm.state.value as TvSplashUiState.NavigationPending).destination)
        }
    }

    @Test
    fun signInActionChoosesItsDestinationAndMatchingAcknowledgementConsumesIt() = runTest(dispatcher) {
        val vm = createViewModel {
            SplashOutcome.Failed(AppFailure.Authentication(AuthenticationKind.REQUIRED))
        }
        advanceUntilIdle()
        vm.onIntent(
            TvSplashIntent.NavigationHandled(
                (vm.state.value as? TvSplashUiState.NavigationPending)?.requestId ?: "stale",
            ),
        )
        assertTrue(vm.state.value is TvSplashUiState.Failed)
        vm.onIntent(TvSplashIntent.SignIn)
        assertEquals(TvSplashDestination.SIGN_IN, (vm.state.value as TvSplashUiState.NavigationPending).destination)
        vm.onIntent(
            TvSplashIntent.NavigationHandled(
                (vm.state.value as? TvSplashUiState.NavigationPending)?.requestId ?: "stale",
            ),
        )
        assertEquals(TvSplashUiState.NavigationComplete, vm.state.value)
    }

    @Test
    fun repeatedRetryWhileLoadingDoesNotStartConcurrentAttempts() = runTest(dispatcher) {
        var calls = 0
        val pending = CompletableDeferred<SplashOutcome>()
        val vm = createViewModel {
            calls++
            pending.await()
        }
        vm.onIntent(TvSplashIntent.Retry)
        runCurrent()
        vm.onIntent(TvSplashIntent.Retry)
        assertEquals(1, calls)
        pending.complete(SplashOutcome.Continue)
        advanceUntilIdle()
        assertTrue(vm.state.value is TvSplashUiState.NavigationPending)
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
        assertTrue(vm.state.value is TvSplashUiState.Failed)
        vm.onIntent(TvSplashIntent.Retry)
        vm.onIntent(TvSplashIntent.Retry)
        advanceUntilIdle()
        assertEquals(2, calls)
        assertTrue(vm.state.value is TvSplashUiState.NavigationPending)
        vm.onIntent(
            TvSplashIntent.NavigationHandled(
                (vm.state.value as? TvSplashUiState.NavigationPending)?.requestId ?: "stale",
            ),
        )
        assertEquals(TvSplashUiState.NavigationComplete, vm.state.value)
    }

    @Test
    fun interruptedAttemptShowsAnErrorAndCanBeRetried() = runTest(dispatcher) {
        var calls = 0
        val vm = createViewModel {
            calls++
            if (calls == 1) SplashOutcome.Failed(AppFailure.OperationInterrupted) else SplashOutcome.Continue
        }
        advanceUntilIdle()
        val failed = vm.state.value as TvSplashUiState.Failed
        assertTrue(ErrorActionKind.RETRY in failed.spec.actions)
        vm.onIntent(TvSplashIntent.Retry)
        advanceUntilIdle()
        assertEquals(2, calls)
        assertTrue(vm.state.value is TvSplashUiState.NavigationPending)
    }

    @Test
    fun configurationAndStorageFailuresDoNotOfferRetry() = runTest(dispatcher) {
        listOf(AppFailure.Configuration, AppFailure.Storage(StorageKind.READ)).forEach { failure ->
            val vm = createViewModel { SplashOutcome.Failed(failure) }
            advanceUntilIdle()
            val failed = vm.state.value as TvSplashUiState.Failed
            assertTrue(ErrorActionKind.RETRY !in failed.spec.actions)
        }
    }

    @Test
    fun staleAcknowledgementDoesNotConsumeANewResultAndNoCollectorIsRequired() = runTest(dispatcher) {
        val vm = createViewModel { SplashOutcome.Continue }
        advanceUntilIdle()
        val pending = vm.state.value as TvSplashUiState.NavigationPending
        vm.onIntent(TvSplashIntent.NavigationHandled("previous-attempt"))
        assertEquals(pending, vm.state.value)
        vm.onIntent(TvSplashIntent.NavigationHandled(pending.requestId))
        vm.onIntent(TvSplashIntent.NavigationHandled(pending.requestId))
        assertEquals(TvSplashUiState.NavigationComplete, vm.state.value)
    }

    private fun createViewModel(outcome: suspend () -> SplashOutcome): TvSplashViewModel {
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
        return TvSplashViewModel(StartSplashUseCase(repository))
    }
}
