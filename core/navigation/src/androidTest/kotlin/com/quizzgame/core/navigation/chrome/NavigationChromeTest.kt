package com.quizzgame.core.navigation.chrome

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.scene.SinglePaneSceneStrategy
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.decoratedEntries
import com.quizzgame.core.navigation.rememberNavigationState
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class NavigationChromeTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val screen get() = ChromeScreen(compose)
    private lateinit var navigator: AppNavigator
    private lateinit var chromeIdentity: Any

    @Test
    fun sceneTransitionsKeepOneChromeAndPreserveRememberStateAcrossHiddenDestinations() {
        compose.setContent { TestHost() }
        screen.assertOneChrome()
        val identity = compose.runOnIdle { chromeIdentity }
        compose.runOnIdle { navigator.navigate(Key.VISIBLE_DETAIL) }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeByFrame()
        screen.assertOneChrome()
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.runOnIdle { assertSame(identity, chromeIdentity) }
        compose.runOnIdle { navigator.navigate(Key.FULLSCREEN) }
        screen.tag(NavigationChromeTestTags.CHROME).assertDoesNotExist()
        compose.runOnIdle { navigator.back() }
        screen.assertOneChrome()
        compose.runOnIdle { assertSame(identity, chromeIdentity) }
    }

    @Test
    fun dialogKeepsTheUnderlyingChromeEvenWithHiddenMetadataAndBackDismissesOnlyTheDialog() {
        compose.setContent { TestHost() }
        compose.runOnIdle { navigator.navigate(Key.DIALOG) }
        screen.tag(NavigationChromeTestTags.route(Key.DIALOG)).assertIsDisplayed()
        screen.assertOneChrome()
        compose.runOnIdle { navigator.back() }
        screen.tag(NavigationChromeTestTags.route(Key.DIALOG)).assertDoesNotExist()
        screen.assertOneChrome()
        compose.runOnIdle { assertEquals(listOf(Key.HOME), navigator.state.snapshot.activeEntries) }
    }

    @Test
    fun savedStateRestoresEveryTabAndEntryStateThroughTheSceneDecorator() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { TestHost() }
        screen.tag(NavigationChromeTestTags.increment(Key.HOME)).performClick()
        compose.runOnIdle {
            navigator.navigate(Key.VISIBLE_DETAIL)
            navigator.switchTopLevel(Key.LIBRARY)
        }
        screen.tag(NavigationChromeTestTags.increment(Key.LIBRARY)).performClick()
        val expected = compose.runOnIdle { navigator.state.snapshot }
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle { assertEquals(expected, navigator.state.snapshot) }
        screen.tag(NavigationChromeTestTags.value(Key.LIBRARY)).assertTextEquals("1")
        compose.runOnIdle {
            navigator.switchTopLevel(Key.HOME)
            navigator.back()
        }
        screen.tag(NavigationChromeTestTags.value(Key.HOME)).assertTextEquals("1")
        screen.assertOneChrome()
    }

    @Test
    fun multiEntrySceneUsesBothPanesWhenTheLastEntryIsNotATopLevelRoot() {
        compose.setContent { TestHost(twoPane = true) }
        compose.runOnIdle { navigator.navigate(Key.DETAIL) }
        screen.tag(NavigationChromeTestTags.route(Key.HOME)).assertIsDisplayed()
        screen.tag(NavigationChromeTestTags.route(Key.DETAIL)).assertIsDisplayed()
        screen.assertOneChrome()
        compose.runOnIdle { navigator.navigate(Key.FULLSCREEN) }
        screen.tag(NavigationChromeTestTags.CHROME).assertDoesNotExist()
        compose.runOnIdle { navigator.back() }
        screen.assertOneChrome()
    }

    @Composable
    private fun TestHost(twoPane: Boolean = false) {
        val state = rememberNavigationState(Key.HOME, listOf(Key.HOME, Key.LIBRARY))
        val current = remember(state) { AppNavigator(state) }
        SideEffect { navigator = current }
        val provider = entryProvider<NavKey> {
            Key.entries.forEach { route ->
                val policy = when (route) {
                    Key.VISIBLE_DETAIL -> navigationChrome(NavigationChromePolicy.VISIBLE)
                    Key.FULLSCREEN, Key.DIALOG -> navigationChrome(NavigationChromePolicy.HIDDEN)
                    else -> emptyMap()
                }
                entry(
                    route,
                    metadata = policy + mapOf("two-pane" to (route == Key.DETAIL)) +
                        if (route == Key.DIALOG) DialogSceneStrategy.dialog() else emptyMap(),
                ) {
                    var value by rememberSaveable { mutableIntStateOf(0) }
                    Column(Modifier.testTag(NavigationChromeTestTags.route(route))) {
                        BasicText("$value", Modifier.testTag(NavigationChromeTestTags.value(route)))
                        BasicText(
                            "Increment",
                            Modifier.testTag(NavigationChromeTestTags.increment(route)).clickable { value++ },
                        )
                    }
                }
            }
        }
        MainNavigationHost(
            entries = state.decoratedEntries(provider),
            placement = NavigationChromePlacement.BOTTOM,
            onBack = { current.back() },
            sceneStrategies = listOfNotNull(
                DialogSceneStrategy(),
                if (twoPane) TwoPaneStrategy else null,
                SinglePaneSceneStrategy(),
            ),
        ) {
            val identity = remember { Any() }
            SideEffect { chromeIdentity = identity }
            Box(Modifier.height(64.dp).testTag(NavigationChromeTestTags.CHROME)) { BasicText("Navigation") }
        }
    }

    private object TwoPaneStrategy : SceneStrategy<NavKey> {
        override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? =
            if (entries.last().metadata["two-pane"] == true) TwoPane(entries.takeLast(2), entries.dropLast(1)) else null
    }

    private data class TwoPane(
        override val entries: List<NavEntry<NavKey>>,
        override val previousEntries: List<NavEntry<NavKey>>,
    ) : Scene<NavKey> {
        override val key: Any = entries.map { it.contentKey }
        override val content: @Composable () -> Unit = {
            Row { entries.forEach { entry -> Box(Modifier.weight(1f)) { entry.Content() } } }
        }
    }

    @Serializable
    private enum class Key : NavKey { HOME, LIBRARY, DETAIL, VISIBLE_DETAIL, FULLSCREEN, DIALOG }
}
