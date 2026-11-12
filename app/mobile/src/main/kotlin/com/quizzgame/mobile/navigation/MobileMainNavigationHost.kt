package com.quizzgame.mobile.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationState
import com.quizzgame.core.navigation.chrome.MainNavigationHost
import com.quizzgame.core.navigation.chrome.NavigationChromePlacement
import com.quizzgame.mobile.R
import com.quizzgame.mobile.navigation.routes.JoinRoomRoute

internal data class MobileTopLevelItem(val route: NavKey, @param:StringRes val label: Int)

internal val MobileTopLevelItems = listOf(MobileTopLevelItem(JoinRoomRoute, R.string.navigation_join_room))

@Composable
internal fun MobileMainNavigationHost(
    state: NavigationState,
    navigator: AppNavigator,
    entries: List<NavEntry<NavKey>>,
    onBack: () -> Unit,
    items: List<MobileTopLevelItem> = MobileTopLevelItems,
    sceneStrategies: List<SceneStrategy<NavKey>> = listOf(DialogSceneStrategy(), SinglePaneSceneStrategy()),
) {
    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val useRail = windowWidth.value >= MediumWindowWidthDp
    MainNavigationHost(
        entries = entries,
        placement = if (useRail) NavigationChromePlacement.START else NavigationChromePlacement.BOTTOM,
        consumedInsets = if (useRail) {
            NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start)
        } else {
            NavigationBarDefaults.windowInsets.only(WindowInsetsSides.Bottom)
        },
        chromeEnabled = useRail || WindowInsets.ime.getBottom(LocalDensity.current) == 0,
        sceneStrategies = sceneStrategies,
        onBack = onBack,
    ) { visible ->
        val select: (NavKey) -> Unit = { if (visible) navigator.switchTopLevel(it) }
        if (useRail) {
            NavigationRail(Modifier.testTag(MobileChromeTags.RAIL)) {
                items.forEach { item ->
                    NavigationRailItem(
                        selected = state.snapshot.active == item.route,
                        onClick = { select(item.route) },
                        icon = { Icon(painterResource(R.drawable.ic_room), contentDescription = null) },
                        label = { Text(stringResource(item.label)) },
                        modifier = Modifier.testTag(MobileChromeTags.item(item.route)),
                    )
                }
            }
        } else {
            NavigationBar(Modifier.testTag(MobileChromeTags.BAR)) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = state.snapshot.active == item.route,
                        onClick = { select(item.route) },
                        icon = { Icon(painterResource(R.drawable.ic_room), contentDescription = null) },
                        label = { Text(stringResource(item.label)) },
                        modifier = Modifier.testTag(MobileChromeTags.item(item.route)),
                    )
                }
            }
        }
    }
}

private const val MediumWindowWidthDp = 600

internal object MobileChromeTags {
    const val BAR = "mobile-navigation-bar"
    const val RAIL = "mobile-navigation-rail"
    fun item(route: NavKey): String = "navigation-item-$route"
}
