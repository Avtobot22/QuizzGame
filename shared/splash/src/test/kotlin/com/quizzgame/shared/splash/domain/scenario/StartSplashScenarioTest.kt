package com.quizzgame.shared.splash.domain.scenario

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.error.failure.StorageKind
import com.quizzgame.core.flags.AppFeatureFlags
import com.quizzgame.core.flags.FeatureFlagsController
import com.quizzgame.core.flags.FeatureFlagsLoadFailure
import com.quizzgame.core.flags.FeatureFlagsLoadResult
import com.quizzgame.core.network.api.ApiCallResult
import com.quizzgame.core.network.api.ApiUnavailableReason
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.network.result.NetworkResult
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.core.session.storage.SessionStorageFailure
import com.quizzgame.shared.splash.data.SplashRemoteDataSource
import com.quizzgame.shared.splash.data.SplashRepositoryImpl
import com.quizzgame.shared.splash.data.dto.AccountDto
import com.quizzgame.shared.splash.data.dto.ProfileDto
import com.quizzgame.shared.splash.data.dto.UserDto
import com.quizzgame.shared.splash.data.mapper.toDomain
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import com.quizzgame.shared.splash.domain.entity.SplashResult
import com.quizzgame.shared.splash.domain.usecase.LoadSplashPolicyUseCase
import com.quizzgame.shared.splash.domain.usecase.LoadSplashUserUseCase
import com.quizzgame.shared.splash.domain.usecase.RefreshSplashFlagsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartSplashScenarioTest {
    @Test
    fun guestLaunchRefreshesFlagsInOrderAndNeverLoadsUser() = runTest {
        val fixture = Fixture()
        assertEquals(SplashResult.Continue, fixture.useCase())
        assertEquals(listOf("launch", "flags"), fixture.calls)
    }

    @Test
    fun guestNeedsSignInWhenFlagIsOff() = runTest {
        val fixture = Fixture().apply { guest = false }
        assertEquals(SplashResult.SignIn, fixture.useCase())
    }

    @Test
    fun authenticatedLaunchChecksUserAfterRefreshingFlags() = runTest {
        val fixture = Fixture().apply { state = SessionState.Authenticated(7) }
        assertEquals(SplashResult.Continue, fixture.useCase())
        assertEquals(listOf("launch", "flags", "user"), fixture.calls)
    }

    @Test
    fun flagsFailureBlocksNavigationEvenWhenOldGuestSnapshotIsTrue() = runTest {
        val fixture = Fixture().apply {
            flagsResult = FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.InvalidRemote)
        }
        assertEquals(SplashResult.Failed(AppFailure.InvalidServerData), fixture.useCase())
        assertEquals(listOf("launch", "flags"), fixture.calls)
    }

    @Test
    fun launchFailureStopsBeforeFlagsAndUser() = runTest {
        val fixture = Fixture().apply { networkFailure = NetworkFailure.Http(503) }
        assertTrue(fixture.useCase() is SplashResult.Failed)
        assertEquals(emptyList<String>(), fixture.calls)
    }

    @Test
    fun generationChangeDuringUserResponseInterruptsStartup() = runTest {
        val fixture = Fixture().apply {
            state = SessionState.Authenticated(1)
            afterUser = { state = SessionState.Authenticated(2) }
        }
        assertEquals(SplashResult.Failed(AppFailure.OperationInterrupted), fixture.useCase())
    }

    @Test
    fun refreshUncertainSessionRoutesToSignInWithoutRequests() = runTest {
        for (state in listOf(SessionState.Blocked(CredentialUnavailableKind.REFRESH_UNCERTAIN))) {
            val fixture = Fixture().apply { this.state = state }
            assertEquals(SplashResult.SignIn, fixture.useCase())
            assertEquals(emptyList<String>(), fixture.calls)
        }
    }

    @Test
    fun sessionExpiryDuringLaunchDoesNotUseGuestFlags() = runTest {
        val fixture = Fixture().apply {
            state = SessionState.Authenticated(1)
            afterLaunch = { state = SessionState.Expired }
        }
        assertEquals(SplashResult.Failed(AppFailure.OperationInterrupted), fixture.useCase())
    }

    @Test
    fun cancellationFromNetworkIsNotConvertedToFailure() = runTest {
        val fixture = Fixture().apply { afterLaunch = { throw CancellationException("cancel") } }
        var cancelled = false
        try { fixture.useCase() } catch (_: CancellationException) { cancelled = true }
        assertTrue(cancelled)
        assertEquals(listOf("launch"), fixture.calls)
    }

    @Test
    fun runtimePreparationFailuresUseTheCommonErrorModel() = runTest {
        val failures = mapOf(
            ApiUnavailableReason.CONFIGURATION to AppFailure.Configuration,
            ApiUnavailableReason.STORAGE to AppFailure.Storage(StorageKind.UNKNOWN),
            ApiUnavailableReason.TRANSPORT to AppFailure.OperationInterrupted,
        )
        failures.forEach { (reason, failure) ->
            val fixture = Fixture().apply { unavailable = reason }
            assertEquals(SplashResult.Failed(failure), fixture.useCase())
            assertEquals(emptyList<String>(), fixture.calls)
        }
    }

    @Test
    fun expiryDuringUserRequestInterruptsStartup() = runTest {
        val fixture = Fixture().apply {
            state = SessionState.Authenticated(1)
            afterUser = { state = SessionState.Expired }
        }
        assertEquals(SplashResult.Failed(AppFailure.OperationInterrupted), fixture.useCase())
    }

    @Test
    fun unavailableSessionPreservesTheStorageFailureAndDoesNotStartRequests() = runTest {
        val fixture = Fixture().apply { state = SessionState.Unavailable(SessionStorageFailure.CRYPTO) }
        assertEquals(SplashResult.Failed(AppFailure.Storage(StorageKind.CRYPTO)), fixture.useCase())
        assertEquals(emptyList<String>(), fixture.calls)
    }

    @Test
    fun closedFlagsControllerFailsInsteadOfLeavingTheAttemptLoading() = runTest {
        val fixture = Fixture().apply { flagsResult = FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.Closed) }
        assertEquals(SplashResult.Failed(AppFailure.OperationInterrupted), fixture.useCase())
        assertEquals(listOf("launch", "flags"), fixture.calls)
    }

    @Test
    fun sessionChangeDuringFlagsStopsBeforeLoadingAnotherSessionsUser() = runTest {
        val fixture = Fixture().apply { afterFlags = { state = SessionState.Authenticated(2) } }
        assertEquals(SplashResult.Failed(AppFailure.OperationInterrupted), fixture.useCase())
        assertEquals(listOf("launch", "flags"), fixture.calls)
    }

    private class Fixture {
        val calls = mutableListOf<String>()
        var state: SessionState = SessionState.Guest
        var guest = true
        var flagsResult: FeatureFlagsLoadResult = FeatureFlagsLoadResult.Applied
        var networkFailure: NetworkFailure? = null
        var unavailable: ApiUnavailableReason? = null
        var afterUser: () -> Unit = {}
        var afterLaunch: () -> Unit = {}
        var afterFlags: () -> Unit = {}
        val reader = SessionStateReader { state }
        private val remote = object : SplashRemoteDataSource {
            override suspend fun launch(): ApiCallResult<Unit> = response {
                calls += "launch"
                afterLaunch()
            }

            override suspend fun currentUser(): ApiCallResult<CurrentUser> = response {
                calls += "user"
                afterUser()
                UserDto(
                    "719278b4-e7af-4e50-af40-25701f2b2b27",
                    AccountDto("2026-11-05T10:30:00Z"),
                    ProfileDto(),
                    emptyList(),
                ).toDomain()
            }
        }
        private suspend fun <T> response(request: suspend () -> T): ApiCallResult<T> {
            val missing = unavailable
            val failed = networkFailure
            return when {
                missing != null -> ApiCallResult.Unavailable(missing)
                failed != null -> ApiCallResult.Executed(NetworkResult.Failure(failed))
                else -> ApiCallResult.Executed(NetworkResult.Success(request()))
            }
        }
        private val repository = SplashRepositoryImpl(
            remote,
            object : AppFeatureFlags { override fun guestAllowed(): Boolean = guest },
            object : FeatureFlagsController {
                override suspend fun refresh(): FeatureFlagsLoadResult {
                    calls += "flags"
                    afterFlags()
                    return flagsResult
                }
            },
            reader,
        )
        val useCase = StartSplashScenario(
            repository,
            LoadSplashPolicyUseCase(repository),
            RefreshSplashFlagsUseCase(repository),
            LoadSplashUserUseCase(repository),
        )
    }
}
