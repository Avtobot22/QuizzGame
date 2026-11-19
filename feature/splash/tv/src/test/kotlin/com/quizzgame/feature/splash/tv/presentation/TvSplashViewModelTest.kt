package com.quizzgame.feature.splash.tv.presentation

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.AuthenticationKind
import com.quizzgame.core.error.failure.ConnectivityKind
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.splash.tv.navigation.TvSplashDestination
import com.quizzgame.shared.splash.domain.entity.SplashResult
import com.quizzgame.shared.splash.domain.repository.SplashRepository
import com.quizzgame.shared.splash.domain.scenario.StartSplashScenario
import com.quizzgame.shared.splash.domain.usecase.LoadSplashPolicyUseCase
import com.quizzgame.shared.splash.domain.usecase.LoadSplashUserUseCase
import com.quizzgame.shared.splash.domain.usecase.RefreshSplashFlagsUseCase
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
            SplashResult.Continue to TvSplashDestination.DASHBOARD,
            SplashResult.SignIn to TvSplashDestination.SIGN_IN,
        )
        destinations.forEach { (result, destination) ->
            val vm = createViewModel { result }
            advanceUntilIdle()
            assertEquals(destination, (vm.state.value as TvSplashUiState.NavigationPending).destination)
            runCurrent()
            assertEquals(destination, (vm.state.value as TvSplashUiState.NavigationPending).destination)
        }
    }

    @Test
    fun signInActionChoosesItsDestinationAndAcknowledgementConsumesIt() = runTest(dispatcher) {
        val vm = createViewModel {
            SplashResult.Failed(AppFailure.Authentication(AuthenticationKind.REQUIRED))
        }
        advanceUntilIdle()
        vm.onIntent(TvSplashIntent.NavigationHandled)
        assertTrue(vm.state.value is TvSplashUiState.Error)
        vm.onIntent(TvSplashIntent.SignIn)
        assertEquals(TvSplashDestination.SIGN_IN, (vm.state.value as TvSplashUiState.NavigationPending).destination)
        vm.onIntent(TvSplashIntent.NavigationHandled)
        assertEquals(TvSplashUiState.NavigationComplete, vm.state.value)
    }

    @Test
    fun repeatedRetryWhileLoadingDoesNotStartConcurrentAttempts() = runTest(dispatcher) {
        var calls = 0
        val pending = CompletableDeferred<SplashResult>()
        val vm = createViewModel {
            calls++
            pending.await()
        }
        vm.onIntent(TvSplashIntent.Retry)
        runCurrent()
        vm.onIntent(TvSplashIntent.Retry)
        assertEquals(1, calls)
        pending.complete(SplashResult.Continue)
        advanceUntilIdle()
        assertTrue(vm.state.value is TvSplashUiState.NavigationPending)
    }

    @Test
    fun retryAfterFailureRunsWholeAttemptOnceAndAcknowledgementConsumesNavigation() = runTest(dispatcher) {
        var calls = 0
        val vm = createViewModel {
            calls++
            if (calls == 1) {
                SplashResult.Failed(AppFailure.Connectivity(ConnectivityKind.TIMEOUT))
            } else {
                SplashResult.Continue
            }
        }
        advanceUntilIdle()
        assertTrue(vm.state.value is TvSplashUiState.Error)
        vm.onIntent(TvSplashIntent.Retry)
        vm.onIntent(TvSplashIntent.Retry)
        advanceUntilIdle()
        assertEquals(2, calls)
        assertTrue(vm.state.value is TvSplashUiState.NavigationPending)
        vm.onIntent(TvSplashIntent.NavigationHandled)
        assertEquals(TvSplashUiState.NavigationComplete, vm.state.value)
    }

    @Test
    fun interruptedAttemptShowsAnErrorAndCanBeRetried() = runTest(dispatcher) {
        var calls = 0
        val vm = createViewModel {
            calls++
            if (calls == 1) SplashResult.Failed(AppFailure.OperationInterrupted) else SplashResult.Continue
        }
        advanceUntilIdle()
        val error = vm.state.value as TvSplashUiState.Error
        assertTrue(ErrorActionKind.RETRY in error.spec.actions)
        vm.onIntent(TvSplashIntent.Retry)
        advanceUntilIdle()
        assertEquals(2, calls)
        assertTrue(vm.state.value is TvSplashUiState.NavigationPending)
    }

    @Test
    fun configurationAndStorageFailuresDoNotOfferRetry() = runTest(dispatcher) {
        listOf(AppFailure.Configuration, AppFailure.Storage(StorageKind.READ)).forEach { failure ->
            val vm = createViewModel { SplashResult.Failed(failure) }
            advanceUntilIdle()
            val error = vm.state.value as TvSplashUiState.Error
            assertTrue(ErrorActionKind.RETRY !in error.spec.actions)
        }
    }

    private fun createViewModel(result: suspend () -> SplashResult): TvSplashViewModel {
        var result: SplashResult = SplashResult.Continue
        val repository = object : SplashRepository {
            override suspend fun currentSession(): SessionState = SessionState.Guest

            override suspend fun loadLaunch(): AppFailure? {
                result = result()
                return (result as? SplashResult.Failed)?.failure
            }

            override suspend fun refreshFlags(): AppFailure? = null
            override suspend fun loadUser(expectedGeneration: Long): AppFailure? =
                error("Guest fixture must not load user")
            override fun guestAllowed(): Boolean = result == SplashResult.Continue
            override fun clearUser() = Unit
        }
        val scenario = StartSplashScenario(
            repository,
            LoadSplashPolicyUseCase(repository),
            RefreshSplashFlagsUseCase(repository),
            LoadSplashUserUseCase(repository),
        )
        return TvSplashViewModel(scenario)
    }
}
