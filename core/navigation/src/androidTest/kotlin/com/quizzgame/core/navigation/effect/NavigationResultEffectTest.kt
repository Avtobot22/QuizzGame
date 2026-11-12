package com.quizzgame.core.navigation.effect

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
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
                }) {
                    delivered += "navigate-$it"
                    true
                }
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
                NavigationResultEffect(1, onHandled = { acknowledged = true }) {
                    visible = false
                    true
                }
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(true, acknowledged) }
    }

    @Test
    fun rejectedNavigationIsNotAcknowledgedAndRetriesOnTheNextResume() {
        val owner = TestOwner()
        var accepted = false
        var acknowledgements = 0
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                NavigationResultEffect(1, onHandled = { acknowledgements++ }) { accepted }
            }
        }
        compose.runOnIdle {
            assertEquals(0, acknowledgements)
            accepted = true
            owner.registry.currentState = Lifecycle.State.STARTED
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, acknowledgements) }
    }

    @Test
    fun recompositionDoesNotRepeatAnAcknowledgedResult() {
        val model = EffectModel()
        var unrelated by mutableStateOf(0)
        var navigations = 0
        var renderedTick = 0
        compose.setContent {
            val tick = unrelated
            SideEffect { renderedTick = tick }
            NavigationResultEffect(model.pending, onHandled = { model.pending = null }) {
                navigations++
                true
            }
        }
        compose.runOnIdle { unrelated++ }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(1, navigations)
            assertEquals(1, renderedTick)
        }
    }

    private class EffectModel {
        var pending by mutableStateOf<Int?>(1)
    }

    private class TestOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
}
