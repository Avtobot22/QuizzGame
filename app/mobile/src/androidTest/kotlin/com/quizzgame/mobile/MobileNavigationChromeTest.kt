package com.quizzgame.mobile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.scene.SinglePaneSceneStrategy
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.chrome.NavigationChromePolicy
import com.quizzgame.core.navigation.chrome.navigationChrome
import com.quizzgame.core.navigation.decoratedEntries
import com.quizzgame.core.navigation.rememberNavigationState
import com.quizzgame.mobile.navigation.MobileChromeTags
import com.quizzgame.mobile.navigation.MobileMainNavigationHost
import com.quizzgame.mobile.navigation.MobileTopLevelItem
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MobileNavigationChromeTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val screen get() = AppShellScreen(compose)
    private lateinit var navigator: AppNavigator
    private var windowWidth by mutableStateOf(400)

    @Test
    fun compactBarAndWideRailShareTheSameTabsAndRetainNestedHistoryAcrossWindowResize() {
        compose.setContent { Host() }
        screen.tag(MobileChromeTags.BAR).assertIsDisplayed()
        screen.tag(MobileChromeTags.RAIL).assertDoesNotExist()
        compose.runOnIdle { navigator.navigate(Key.DETAIL) }
        screen.tag(MobileChromeTags.BAR).assertDoesNotExist()
        compose.runOnIdle { navigator.back() }
        screen.tag(MobileChromeTags.item(Key.LIBRARY)).performClick()
        compose.runOnIdle { assertEquals(Key.LIBRARY, navigator.state.snapshot.active) }
        compose.runOnIdle { windowWidth = 840 }
        screen.tag(MobileChromeTags.RAIL).assertIsDisplayed()
        screen.tag(MobileChromeTags.BAR).assertDoesNotExist()
        screen.tag(MobileChromeTags.item(Key.HOME)).performClick()
        compose.runOnIdle { assertEquals(Key.HOME, navigator.state.snapshot.active) }
        compose.onAllNodesWithTag(MobileChromeTags.RAIL).assertCountEquals(1)
    }

    @Test
    fun explicitVisibleDestinationKeepsChromeAndReselectionUsesExistingPopToRootPolicy() {
        compose.setContent { Host() }
        compose.runOnIdle { navigator.navigate(Key.KEEP_CHROME) }
        screen.tag(MobileChromeTags.BAR).assertIsDisplayed()
        screen.tag(MobileChromeTags.item(Key.HOME)).performClick()
        compose.runOnIdle { assertEquals(listOf(Key.HOME), navigator.state.snapshot.activeEntries) }
        screen.tag(MobileNavigationChromeTestTags.route(Key.HOME)).assertIsDisplayed()
    }

    @Test
    fun modalBottomSheetKeepsTheBackgroundNavigationBarAndDismissesIndependently() {
        compose.setContent { Host() }
        compose.runOnIdle { navigator.navigate(Key.SHEET) }
        screen.tag(MobileNavigationChromeTestTags.SHEET_CONTENT).assertIsDisplayed()
        compose.onAllNodesWithTag(MobileChromeTags.BAR).assertCountEquals(1)
        compose.runOnIdle { navigator.back() }
        screen.tag(MobileNavigationChromeTestTags.SHEET_CONTENT).assertDoesNotExist()
        screen.tag(MobileChromeTags.BAR).assertIsDisplayed()
    }

    @Composable
    private fun Host() {
        val actualWindow = LocalWindowInfo.current
        val density = LocalDensity.current
        val widthPx = with(density) { windowWidth.dp.roundToPx() }
        val windowInfo = object : WindowInfo by actualWindow {
            override val containerSize = IntSize(widthPx, actualWindow.containerSize.height)
        }
        QuizzMobileTheme {
            CompositionLocalProvider(LocalWindowInfo provides windowInfo) {
                val state = rememberNavigationState(Key.HOME, listOf(Key.HOME, Key.LIBRARY))
                val current = remember(state) { AppNavigator(state) }
                SideEffect { navigator = current }
                val provider = entryProvider<NavKey> {
                    Key.entries.forEach { route ->
                        entry(
                            route,
                            metadata = mapOf("sheet" to (route == Key.SHEET)) +
                                if (route == Key.KEEP_CHROME) {
                                    navigationChrome(NavigationChromePolicy.VISIBLE)
                                } else {
                                    emptyMap()
                                },
                        ) {
                            QuizzScreenScaffold { padding ->
                                Column(Modifier.padding(padding).testTag(MobileNavigationChromeTestTags.route(route))) {
                                    Text(route.name)
                                }
                            }
                        }
                    }
                }
                MobileMainNavigationHost(
                    state,
                    current,
                    state.decoratedEntries(provider),
                    onBack = { current.back() },
                    sceneStrategies = listOf(SheetStrategy, DialogSceneStrategy(), SinglePaneSceneStrategy()),
                    items = listOf(
                        MobileTopLevelItem(Key.HOME, R.string.navigation_join_room),
                        MobileTopLevelItem(Key.LIBRARY, R.string.navigation_join_room),
                    ),
                )
            }
        }
    }

    private object SheetStrategy : SceneStrategy<NavKey> {
        override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? =
            if (entries.last().metadata["sheet"] == true) {
                SheetScene(entries.last(), entries.dropLast(1), onBack)
            } else {
                null
            }
    }

    private data class SheetScene(
        val entry: NavEntry<NavKey>,
        override val overlaidEntries: List<NavEntry<NavKey>>,
        val onBack: () -> Unit,
    ) : OverlayScene<NavKey> {
        override val key: Any = entry.contentKey
        override val entries = listOf(entry)
        override val previousEntries = overlaidEntries

        @OptIn(ExperimentalMaterial3Api::class)
        override val content: @Composable () -> Unit = {
            ModalBottomSheet(onDismissRequest = onBack) {
                Text("Sheet", Modifier.testTag(MobileNavigationChromeTestTags.SHEET_CONTENT))
                entry.Content()
            }
        }
    }

    @Serializable
    private enum class Key : NavKey { HOME, LIBRARY, DETAIL, KEEP_CHROME, SHEET }
}
