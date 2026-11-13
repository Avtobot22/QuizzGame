package com.quizzgame.shared.splash.domain

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
import com.quizzgame.shared.splash.data.CurrentUserStore
import com.quizzgame.shared.splash.data.SplashRemoteDataSource
import com.quizzgame.shared.splash.data.SplashRepositoryImpl
import com.quizzgame.shared.splash.data.dto.AccountDto
import com.quizzgame.shared.splash.data.dto.ProfileDto
import com.quizzgame.shared.splash.data.dto.UserDto
import com.quizzgame.shared.splash.data.mapper.toDomain
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import com.quizzgame.shared.splash.domain.entity.SplashOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StartSplashUseCaseTest {
    @Test
    fun guestLaunchRefreshesFlagsInOrderAndNeverLoadsUser() = runTest {
        val fixture = Fixture()
        assertEquals(SplashOutcome.Continue, fixture.useCase())
        assertEquals(listOf("launch", "flags"), fixture.calls)
    }

    @Test
    fun guestPermissionIsDeniedWhenFlagIsOff() = runTest {
        val fixture = Fixture().apply { guest = false }
        assertEquals(SplashOutcome.SignIn, fixture.useCase())
    }

    @Test
    fun authenticatedLaunchLoadsRequiredUserAndStoresForCurrentGeneration() = runTest {
        val fixture = Fixture().apply { state = SessionState.Authenticated(7) }
        assertEquals(SplashOutcome.Continue, fixture.useCase())
        assertEquals(listOf("launch", "flags", "user"), fixture.calls)
        assertEquals("719278b4-e7af-4e50-af40-25701f2b2b27", fixture.users.current()?.id.toString())
    }

    @Test
    fun flagsFailureBlocksNavigationEvenWhenOldGuestSnapshotIsTrue() = runTest {
        val fixture = Fixture().apply {
            flagsResult = FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.InvalidRemote)
        }
        assertEquals(SplashOutcome.Failed(AppFailure.InvalidServerData), fixture.useCase())
        assertEquals(listOf("launch", "flags"), fixture.calls)
    }

    @Test
    fun launchFailureStopsBeforeFlagsAndUser() = runTest {
        val fixture = Fixture().apply { networkFailure = NetworkFailure.Http(503) }
        assertTrue(fixture.useCase() is SplashOutcome.Failed)
        assertEquals(emptyList<String>(), fixture.calls)
    }

    @Test
    fun generationChangeDuringUserResponseNeverPublishesOldProfile() = runTest {
        val fixture = Fixture().apply {
            state = SessionState.Authenticated(1)
            afterUser = { state = SessionState.Authenticated(2) }
        }
        assertEquals(SplashOutcome.Failed(AppFailure.OperationInterrupted), fixture.useCase())
        assertNull(fixture.users.current())
    }

    @Test
    fun logoutHidesStoredProfileFromPreviousSession() = runTest {
        val fixture = Fixture().apply { state = SessionState.Authenticated(1) }
        fixture.useCase()
        fixture.state = SessionState.Guest
        assertNull(fixture.users.current())
    }

    @Test
    fun expiredAndRefreshUncertainSessionsRouteToSignInWithoutRequests() = runTest {
        for (state in listOf(SessionState.Expired, SessionState.Blocked(CredentialUnavailableKind.REFRESH_UNCERTAIN))) {
            val fixture = Fixture().apply { this.state = state }
            assertEquals(SplashOutcome.SignIn, fixture.useCase())
            assertEquals(emptyList<String>(), fixture.calls)
        }
    }

    @Test
    fun sessionExpiryDuringLaunchDoesNotUseGuestFlags() = runTest {
        val fixture = Fixture().apply {
            state = SessionState.Authenticated(1)
            afterLaunch = { state = SessionState.Expired }
        }
        assertEquals(SplashOutcome.SignIn, fixture.useCase())
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
            assertEquals(SplashOutcome.Failed(failure), fixture.useCase())
            assertEquals(emptyList<String>(), fixture.calls)
        }
    }

    @Test
    fun expiryDuringUserRequestRoutesToSignInWithoutPublishingProfile() = runTest {
        val fixture = Fixture().apply {
            state = SessionState.Authenticated(1)
            afterUser = { state = SessionState.Expired }
        }
        assertEquals(SplashOutcome.SignIn, fixture.useCase())
        assertNull(fixture.users.current())
    }

    @Test
    fun unavailableSessionPreservesTheStorageFailureAndDoesNotStartRequests() = runTest {
        val fixture = Fixture().apply { state = SessionState.Unavailable(SessionStorageFailure.CRYPTO) }
        assertEquals(SplashOutcome.Failed(AppFailure.Storage(StorageKind.CRYPTO)), fixture.useCase())
        assertEquals(emptyList<String>(), fixture.calls)
    }

    @Test
    fun closedFlagsControllerFailsInsteadOfLeavingTheAttemptLoading() = runTest {
        val fixture = Fixture().apply { flagsResult = FeatureFlagsLoadResult.Failed(FeatureFlagsLoadFailure.Closed) }
        assertEquals(SplashOutcome.Failed(AppFailure.OperationInterrupted), fixture.useCase())
        assertEquals(listOf("launch", "flags"), fixture.calls)
    }

    @Test
    fun sessionChangeDuringFlagsStopsBeforeLoadingAnotherSessionsUser() = runTest {
        val fixture = Fixture().apply { afterFlags = { state = SessionState.Authenticated(2) } }
        assertEquals(SplashOutcome.Failed(AppFailure.OperationInterrupted), fixture.useCase())
        assertEquals(listOf("launch", "flags"), fixture.calls)
        assertNull(fixture.users.current())
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
        val users = CurrentUserStore(reader)
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
            users,
        )
        val useCase = StartSplashUseCase(repository)
    }
}
