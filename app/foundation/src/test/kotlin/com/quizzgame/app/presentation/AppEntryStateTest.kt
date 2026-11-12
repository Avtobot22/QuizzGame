package com.quizzgame.app.presentation

import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppEntryStateTest {
    @Test
    fun guestAndAuthenticatedSessionsBothStartFromTheSplashFeature() {
        listOf(SessionState.Guest, SessionState.Authenticated(7)).forEach { session ->
            val entry = appEntryState(session, 0) as AppEntryState.Ready
            assertEquals(AppEntryScreen.SPLASH, entry.screen)
        }
    }

    @Test
    fun expiredAndUncertainSessionsRequireSignInRatherThanContentOrAnotherRefresh() {
        listOf(SessionState.Expired, SessionState.Blocked(CredentialUnavailableKind.REFRESH_UNCERTAIN)).forEach {
            assertEquals(AppEntryScreen.SIGN_IN, (appEntryState(it, 0) as AppEntryState.Ready).screen)
        }
    }

    @Test
    fun storageClosedAndTerminatingSessionsProduceBlockingErrorsInsteadOfAUsableGraph() {
        val blocked = listOf(
            SessionState.Closed,
            SessionState.Unavailable(SessionStorageFailure.CRYPTO),
            SessionState.Blocked(CredentialUnavailableKind.STORAGE_UNAVAILABLE),
            SessionState.Blocked(CredentialUnavailableKind.NOT_INITIALIZED),
            SessionState.Blocked(CredentialUnavailableKind.TERMINATING),
            SessionState.Blocked(CredentialUnavailableKind.CLOSED),
        )
        blocked.forEach { session ->
            val entry = appEntryState(session, 0) as AppEntryState.Blocked
            assertFalse(ErrorActionKind.RETRY in entry.error.actions)
            assertTrue(entry.error.title in setOf(ErrorCopyKey.STORAGE_PROBLEM, ErrorCopyKey.GENERIC_FAILURE))
        }
        assertEquals(AppEntryState.Waiting, appEntryState(SessionState.Initializing, 0))
    }

    @Test
    fun graphIdentitySurvivesRepeatedObservationButNotLogoutNewSessionOrNewProcess() {
        val initial = appEntryState(SessionState.Authenticated(1), 0, "process-a")
        assertEquals(initial, appEntryState(SessionState.Authenticated(1), 0, "process-a"))
        assertNotEquals(initial, appEntryState(SessionState.Authenticated(2), 0, "process-a"))
        assertNotEquals(initial, appEntryState(SessionState.Guest, 0, "process-a"))
        assertNotEquals(initial, appEntryState(SessionState.Authenticated(1), 0, "process-b"))
        assertNotEquals(
            appEntryState(SessionState.Guest, 0, "process-a"),
            appEntryState(SessionState.Guest, 1, "process-a"),
        )
    }
}
