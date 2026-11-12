package com.quizzgame.core.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavigationRestorationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun savedInstanceStateRestoresEverySectionTheActiveSectionAndApplicationFlow() {
        val restoration = StateRestorationTester(compose)
        lateinit var navigator: AppNavigator
        restoration.setContent {
            val state = rememberNavigationState(Route.HOME, listOf(Route.HOME, Route.LIBRARY))
            SideEffect { navigator = AppNavigator(state) }
        }
        compose.runOnIdle {
            navigator.navigate(Route.DETAIL)
            navigator.switchTopLevel(Route.LIBRARY)
            navigator.navigate(Route.OTHER)
            navigator.showFlow(Route.AUTH)
        }
        val expected = compose.runOnIdle { navigator.state.snapshot }
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle { assertEquals(expected, navigator.state.snapshot) }
    }

    @Test
    fun equalDestinationsInDifferentSectionsHaveIndependentViewModelsRetainedUntilPopOrReset() {
        lateinit var navigator: AppNavigator
        lateinit var visibleModel: EntryModel
        compose.setContent {
            val state = rememberNavigationState(Route.HOME, listOf(Route.HOME, Route.LIBRARY))
            SideEffect { navigator = AppNavigator(state) }
            val provider = entryProvider<NavKey> {
                entry<Route> {
                    val model = viewModel<EntryModel>()
                    SideEffect { visibleModel = model }
                }
            }
            NavDisplay(entries = state.decoratedEntries(provider), onBack = { navigator.back() })
        }
        compose.runOnIdle { navigator.navigate(Route.DETAIL) }
        compose.waitForIdle()
        val first = compose.runOnIdle { visibleModel }
        compose.runOnIdle {
            navigator.switchTopLevel(Route.LIBRARY)
            navigator.navigate(Route.DETAIL)
        }
        compose.waitForIdle()
        compose.runOnIdle { assertNotSame(first, visibleModel) }
        compose.runOnIdle { navigator.switchTopLevel(Route.HOME) }
        compose.waitForIdle()
        compose.runOnIdle { assertSame(first, visibleModel) }
        compose.runOnIdle { navigator.back() }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(first.cleared) }
        compose.runOnIdle { navigator.navigate(Route.DETAIL) }
        compose.waitForIdle()
        val reopened = compose.runOnIdle { visibleModel }
        compose.runOnIdle {
            assertNotSame(first, reopened)
            navigator.replaceRoot(Route.HOME)
        }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(reopened.cleared) }
    }

    class EntryModel : ViewModel() {
        var cleared = false
            private set
        override fun onCleared() { cleared = true }
    }

    @Serializable
    enum class Route : NavKey { HOME, LIBRARY, DETAIL, OTHER, AUTH }
}
