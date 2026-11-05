package com.quizzgame.core.navigation.effect

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NavigationResultEffectTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pendingResultWaitsForResumedAndIsAcknowledgedBeforeAnotherResume() {
        val owner = TestOwner()
        var result by mutableStateOf<Int?>(1)
        val delivered = mutableListOf<String>()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                NavigationResultEffect(result, onHandled = {
                    delivered += "handled-$it"
                    result = null
                }) { delivered += "navigate-$it" }
            }
        }
        compose.runOnIdle {
            assertEquals(emptyList<String>(), delivered)
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(listOf("navigate-1", "handled-1"), delivered)
            owner.registry.currentState = Lifecycle.State.STARTED
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(listOf("navigate-1", "handled-1"), delivered) }
    }

    @Test
    fun removingTheSourceEntryDuringNavigationStillAcknowledgesItsResult() {
        var visible by mutableStateOf(true)
        var acknowledged = false
        compose.setContent {
            if (visible) {
                NavigationResultEffect(1, onHandled = { acknowledged = true }) { visible = false }
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(true, acknowledged) }
    }

    private class TestOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
}
