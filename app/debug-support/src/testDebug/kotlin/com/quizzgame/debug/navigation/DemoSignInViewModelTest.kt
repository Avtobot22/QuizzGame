package com.quizzgame.debug.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DemoSignInViewModelTest {
    @Test
    fun completionRemainsAvailableWithoutUiCollectorsUntilAcknowledged() {
        val model = DemoSignInViewModel()
        model.requestRestart()
        assertEquals(DemoSignInResult.RESTART_REQUESTED, model.result.value)
        model.resultHandled(DemoSignInResult.RESTART_REQUESTED)
        assertNull(model.result.value)
        model.requestRestart()
        assertEquals(DemoSignInResult.RESTART_REQUESTED, model.result.value)
    }
}
