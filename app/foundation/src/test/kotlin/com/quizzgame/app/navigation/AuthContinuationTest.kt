package com.quizzgame.app.navigation

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class AuthContinuationTest {
    @Test
    fun commitsToNewOwnerOnceEvenAfterStorageRecovery() {
        val state = AuthContinuation()
        state.begin(AuthReturnTarget.Profile, 0)
        state.sessionChanged(SessionState.Unavailable(SessionStorageFailure.WRITE), 10)
        assertNull(state.ready.value)
        state.sessionChanged(SessionState.Authenticated(19), 15)
        val delivered = requireNotNull(state.ready.value)
        assertEquals(AuthReturnTarget.Profile, delivered.target)
        assertEquals(19L, delivered.generation)
        state.acknowledge(UUID.randomUUID())
        assertEquals(delivered, state.ready.value)
        state.acknowledge(delivered.id)
        state.sessionChanged(SessionState.Authenticated(19), 20)
        assertNull(state.ready.value)
    }

    @Test
    fun cancellationAndExpiryDiscardTheTarget() {
        val state = AuthContinuation()
        state.begin(AuthReturnTarget.QuizEntry("entry"), 5)
        state.cancel()
        state.sessionChanged(SessionState.Authenticated(1), 30)
        assertNull(state.ready.value)
        state.begin(AuthReturnTarget.Home, 0)
        state.sessionChanged(SessionState.Authenticated(2), 1_000_000)
        assertNull(state.ready.value)
    }
}
