package com.quizzgame.app.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationSnapshot
import com.quizzgame.core.navigation.NavigationState
import com.quizzgame.core.navigation.policy.DestinationAccess
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.core.session.lifecycle.SessionState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationCoordinatorTest {
    private var session: SessionState = SessionState.Guest
    private val history = NavigationState(NavigationSnapshot(Key.HOME, flow = listOf(Key.SPLASH)))
    private val checkpoint = mutableStateOf(NavigationCheckpoint())
    private val coordinator = createCoordinator()

    @Test
    fun protectedRequestWaitsForAuthAndSplashThenIsAcknowledgedAfterDelivery() {
        synchronize()
        val request = request("invite")
        assertTrue(coordinator.open(request))
        assertEquals(request, coordinator.state.pending)
        assertEquals(listOf(Key.AUTH), history.snapshot.flow)
        session = SessionState.Authenticated(1)
        coordinator.synchronize("authenticated", "user-1", false)
        assertEquals(request, coordinator.state.pending)
        assertEquals(listOf(Key.SPLASH), history.snapshot.flow)
        assertTrue(coordinator.splashCompleted())
        assertEquals(listOf(Key.HOME, Key.ROOM), history.snapshot.activeEntries)
        assertNull(coordinator.state.pending)
        assertEquals("invite", coordinator.state.handledRequestId)
        coordinator.open(request)
        assertEquals(listOf(Key.HOME, Key.ROOM), history.snapshot.activeEntries)
    }

    @Test
    fun newRequestDuringAuthReplacesPendingAndCancellationDoesNotGrantAdmission() {
        synchronize()
        coordinator.open(request("first"))
        val latest = request("latest", Key.SECOND_ROOM)
        coordinator.open(latest)
        assertEquals(latest, coordinator.state.pending)
        assertFalse(coordinator.cancelAuthentication())
        assertNull(coordinator.state.pending)
        assertFalse(coordinator.state.admitted)
        assertEquals(listOf(Key.AUTH), history.snapshot.flow)
    }

    @Test
    fun optionalAuthCancellationReturnsToTheOriginalPublicHistory() {
        synchronize()
        coordinator.splashCompleted()
        coordinator.navigator.navigate(Key.PUBLIC_DETAIL)
        coordinator.open(request("invite"))
        assertTrue(coordinator.back())
        assertEquals(listOf(Key.HOME, Key.PUBLIC_DETAIL), history.snapshot.visibleEntries)
        assertNull(coordinator.state.pending)
    }

    @Test
    fun logoutAndExpirationClearEveryPrivateHistoryAndPendingRequest() {
        session = SessionState.Authenticated(1)
        coordinator.synchronize("auth-1", "user-1", false)
        coordinator.splashCompleted()
        coordinator.open(request("invite"))
        session = SessionState.Expired
        coordinator.synchronize("expired", "expired", true)
        assertEquals(listOf(Key.AUTH), history.snapshot.flow)
        assertEquals(listOf(Key.HOME), history.snapshot.activeEntries)
        assertNull(coordinator.state.pending)
        assertFalse(coordinator.state.admitted)
        session = SessionState.Guest
        coordinator.synchronize("logout", "guest:1", false)
        assertEquals(listOf(Key.SPLASH), history.snapshot.flow)
        assertNull(coordinator.state.handledRequestId)
    }

    @Test
    fun guestToGuestLogoutStillInvalidatesTheNavigationScope() {
        synchronize()
        coordinator.splashCompleted()
        coordinator.navigator.navigate(Key.PUBLIC_DETAIL)
        val generation = history.snapshot.generation
        coordinator.synchronize("guest-logout", "guest:1", false)
        assertTrue(history.snapshot.generation > generation)
        assertEquals(listOf(Key.HOME), history.snapshot.activeEntries)
    }

    @Test
    fun savedPendingSurvivesANewCoordinatorAndAProcessRevalidation() {
        synchronize()
        val pending = request("pending")
        coordinator.open(pending)
        checkpoint.value = Json.decodeFromString<NavigationCheckpoint>(Json.encodeToString(checkpoint.value))
        val restored = createCoordinator()
        restored.synchronize("process-2", "guest", false)
        assertEquals(pending, restored.state.pending)
        restored.splashCompleted()
        assertEquals(listOf(Key.AUTH), history.snapshot.flow)
        assertEquals(pending, restored.state.pending)
    }

    @Test
    fun newProcessRechecksSplashWithoutDestroyingPermittedSectionHistory() {
        synchronize()
        coordinator.splashCompleted()
        coordinator.navigator.navigate(Key.PUBLIC_DETAIL)
        val restored = createCoordinator()
        restored.synchronize("process-2", "guest", false)
        assertFalse(restored.state.admitted)
        assertEquals(listOf(Key.SPLASH), history.snapshot.visibleEntries)
        restored.splashCompleted()
        assertEquals(listOf(Key.HOME, Key.PUBLIC_DETAIL), history.snapshot.visibleEntries)
    }

    @Test
    fun missingSessionNeverRestoresProtectedContentOrAdmitsExpiredSession() {
        session = SessionState.Authenticated(1)
        coordinator.synchronize("auth-1", "user-1", false)
        coordinator.splashCompleted()
        coordinator.open(request("invite"))
        session = SessionState.Expired
        coordinator.synchronize("process-2", "expired", true)
        assertFalse(coordinator.splashCompleted())
        assertEquals(listOf(Key.AUTH), history.snapshot.visibleEntries)
        assertEquals(listOf(Key.HOME), history.snapshot.activeEntries)
    }

    @Test
    fun unknownDestinationsAndInvalidSectionTargetsAreRejected() {
        synchronize()
        assertFalse(coordinator.open(request("unknown", Key.UNKNOWN)))
        assertFalse(coordinator.open(NavigationRequest("wrong-section", Key.ROOM, Key.UNKNOWN)))
        assertNull(coordinator.state.pending)
    }

    @Test
    fun sameRuntimeIdentityDoesNotRestartSplashOnConfigurationChange() {
        synchronize()
        coordinator.splashCompleted()
        coordinator.navigator.navigate(Key.PUBLIC_DETAIL)
        synchronize()
        assertTrue(coordinator.state.admitted)
        assertEquals(listOf(Key.HOME, Key.PUBLIC_DETAIL), history.snapshot.visibleEntries)
    }

    private fun synchronize() { coordinator.synchronize("process-1", "guest", false) }
    private fun request(id: String, destination: NavKey = Key.ROOM) = NavigationRequest(id, destination, Key.HOME)
    private fun createCoordinator() = NavigationCoordinator(
        AppNavigator(history),
        Key.SPLASH,
        Key.AUTH,
        { session },
        { destination ->
            when (destination) {
                Key.HOME, Key.PUBLIC_DETAIL -> DestinationAccess.PUBLIC
                Key.ROOM, Key.SECOND_ROOM -> DestinationAccess.AUTHENTICATED
                else -> DestinationAccess.DENIED
            }
        },
        checkpoint,
    )

    @Serializable
    enum class Key : NavKey { HOME, SPLASH, AUTH, ROOM, SECOND_ROOM, PUBLIC_DETAIL, UNKNOWN }
}
