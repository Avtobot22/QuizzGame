package com.quizzgame.debug.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DemoSignInViewModelTest {
    @Test
    fun completionRemainsAvailableWithoutUiCollectorsUntilAcknowledged() {
        val model = DemoSignInViewModel()
        model.continueAsGuest()
        assertEquals(DemoSignInResult.GUEST_ACCESS_ALLOWED, model.result.value)
        model.resultHandled(DemoSignInResult.GUEST_ACCESS_ALLOWED)
        assertNull(model.result.value)
        model.continueAsGuest()
        assertEquals(DemoSignInResult.GUEST_ACCESS_ALLOWED, model.result.value)
    }
}
