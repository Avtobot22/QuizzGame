package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationState
import com.quizzgame.core.navigation.chrome.MainNavigationHost
import com.quizzgame.core.navigation.chrome.NavigationChromePlacement
import com.quizzgame.feature.dashboard.mobile.domain.entity.NavigationAppearance

@Composable
internal fun MobileMainNavigationHost(
    state: NavigationState,
    navigator: AppNavigator,
    entries: List<NavEntry<NavKey>>,
    onBack: () -> Unit,
    items: List<MobileTopLevelItem> = MobileTopLevelItems,
    appearance: NavigationAppearance = NavigationAppearance(),
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
        chromeEnabled = items.size > 1 && (useRail || WindowInsets.ime.getBottom(LocalDensity.current) == 0),
        sceneStrategies = sceneStrategies,
        onBack = onBack,
    ) { visible ->
        val select: (NavKey) -> Unit = { if (visible) navigator.switchTopLevel(it) }
        MobileNavigationChrome(useRail, state.snapshot.active, items, appearance, select)
    }
}

private const val MediumWindowWidthDp = 600
