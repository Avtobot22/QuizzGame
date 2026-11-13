package com.quizzgame.core.navigation.chrome

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay

@Composable
fun MainNavigationHost(
    entries: List<NavEntry<NavKey>>,
    placement: NavigationChromePlacement,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    consumedInsets: WindowInsets = WindowInsets(0),
    sceneContentModifier: Modifier = Modifier,
    chromeEnabled: Boolean = true,
    sceneStrategies: List<SceneStrategy<NavKey>> = listOf(DialogSceneStrategy(), SinglePaneSceneStrategy()),
    chrome: @Composable (visible: Boolean) -> Unit,
) {
    SharedTransitionLayout(modifier.fillMaxSize()) {
        val decorator = rememberNavigationChromeSceneDecorator(
            placement = placement,
            sharedScope = this,
            consumedInsets = consumedInsets,
            sceneContentModifier = sceneContentModifier,
            chromeEnabled = chromeEnabled,
            chrome = chrome,
        )
        NavDisplay(
            entries = entries,
            sceneStrategies = sceneStrategies,
            sceneDecoratorStrategies = listOf(decorator),
            sharedTransitionScope = this,
            onBack = onBack,
        )
    }
}
