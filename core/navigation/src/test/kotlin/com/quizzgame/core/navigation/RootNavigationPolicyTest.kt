package com.quizzgame.core.navigation

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RootNavigationPolicyTest {
    @Test
    fun unknownRefreshRequiresSignInAndTerminationBlocksPrivateContent() {
        GuestAccess.entries.forEach { permission ->
            assertEquals(
                RootNavigationDecision.SIGN_IN,
                decideRootNavigation(SessionState.Blocked(CredentialUnavailableKind.REFRESH_UNCERTAIN), permission),
            )
            assertEquals(
                RootNavigationDecision.BLOCKED,
                decideRootNavigation(SessionState.Blocked(CredentialUnavailableKind.TERMINATING), permission),
            )
        }
    }

    @Test
    fun guestAccessIsAnExplicitBusinessPermissionRatherThanAnAuthenticationAssumption() {
        assertEquals(RootNavigationDecision.SPLASH, decideRootNavigation(SessionState.Guest, GuestAccess.UNKNOWN))
        assertEquals(RootNavigationDecision.CONTENT, decideRootNavigation(SessionState.Guest, GuestAccess.ALLOWED))
        assertEquals(RootNavigationDecision.SIGN_IN, decideRootNavigation(SessionState.Guest, GuestAccess.DENIED))
    }

    @Test
    fun bootstrapStorageAndExpiryNeverOpenContentEvenWhenGuestAccessIsAllowed() {
        GuestAccess.entries.forEach { permission ->
            assertEquals(
                RootNavigationDecision.BOOTSTRAP,
                decideRootNavigation(SessionState.Initializing, permission),
            )
            assertEquals(
                RootNavigationDecision.BLOCKED,
                decideRootNavigation(SessionState.Unavailable(SessionStorageFailure.CRYPTO), permission),
            )
            assertEquals(RootNavigationDecision.BLOCKED, decideRootNavigation(SessionState.Closed, permission))
            assertEquals(RootNavigationDecision.SIGN_IN, decideRootNavigation(SessionState.Expired, permission))
            assertEquals(
                RootNavigationDecision.CONTENT,
                decideRootNavigation(SessionState.Authenticated(1), permission),
            )
        }
    }

    @Test
    fun authorizationEpochChangesOnLogoutAndNewSessionButNotOnRepeatedObservation() {
        val authenticated = sessionNavigationEpoch(SessionState.Authenticated(1))
        assertEquals(authenticated, sessionNavigationEpoch(SessionState.Authenticated(1)))
        assertNotEquals(authenticated, sessionNavigationEpoch(SessionState.Authenticated(2)))
        assertNotEquals(authenticated, sessionNavigationEpoch(SessionState.Guest))
        assertNotEquals(authenticated, sessionNavigationEpoch(SessionState.Expired))
    }
}
