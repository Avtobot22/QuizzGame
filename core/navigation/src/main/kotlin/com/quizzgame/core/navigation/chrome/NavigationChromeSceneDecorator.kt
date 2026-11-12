package com.quizzgame.core.navigation.chrome

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlin.math.roundToInt

@Composable
internal fun rememberNavigationChromeSceneDecorator(
    placement: NavigationChromePlacement,
    sharedScope: SharedTransitionScope,
    consumedInsets: WindowInsets,
    sceneContentModifier: Modifier,
    chromeEnabled: Boolean,
    chrome: @Composable (visible: Boolean) -> Unit,
): SceneDecoratorStrategy<NavKey> {
    val currentChrome by rememberUpdatedState(chrome)
    val layout = rememberUpdatedState(ChromeLayout(placement, consumedInsets, sceneContentModifier, chromeEnabled))
    val movableChrome = remember {
        movableContentOf<Boolean> { visible ->
            ChromeVisibilityLayout(visible, layout.value.placement, sharedScope) { currentChrome(visible) }
        }
    }
    return remember(sharedScope) {
        NavigationChromeSceneDecorator(sharedScope, layout, movableChrome)
    }
}

private class NavigationChromeSceneDecorator(
    private val sharedScope: SharedTransitionScope,
    private val layout: State<ChromeLayout>,
    private val chrome: @Composable (Boolean) -> Unit,
) : SceneDecoratorStrategy<NavKey> {
    override fun SceneDecoratorStrategyScope<NavKey>.decorateScene(scene: Scene<NavKey>): Scene<NavKey> =
        NavigationChromeScene(
            scene = scene,
            sceneChromeVisible = chromeVisible(scene.entries),
            sharedScope = sharedScope,
            layout = layout,
            chrome = chrome,
        )
}

private data class NavigationChromeScene(
    val scene: Scene<NavKey>,
    val sceneChromeVisible: Boolean,
    val sharedScope: SharedTransitionScope,
    val layout: State<ChromeLayout>,
    val chrome: @Composable (Boolean) -> Unit,
) : Scene<NavKey> by scene {
    override val key: Any = scene::class to scene.key

    override val content: @Composable () -> Unit = {
        val configuration = layout.value
        val visible = sceneChromeVisible && configuration.enabled
        val animatedScope = LocalNavAnimatedContentScope.current
        val isOwner = animatedScope.transition.targetState == EnterExitState.Visible
        val chromeContent: @Composable () -> Unit = {
            with(sharedScope) {
                Box(
                    Modifier.cacheChromeSize(!isOwner).sharedElement(
                        rememberSharedContentState("navigation-chrome"),
                        animatedVisibilityScope = animatedScope,
                    ),
                ) {
                    if (isOwner) chrome(visible)
                }
            }
        }
        val contentModifier = configuration.sceneContentModifier.then(
            if (visible) Modifier.consumeWindowInsets(configuration.consumedInsets) else Modifier,
        )
        when (configuration.placement) {
            NavigationChromePlacement.START -> Row(Modifier.fillMaxSize()) {
                chromeContent()
                Box(contentModifier.weight(1f)) { scene.content() }
            }
            NavigationChromePlacement.TOP -> Column(Modifier.fillMaxSize()) {
                chromeContent()
                Box(contentModifier.weight(1f)) { scene.content() }
            }
            NavigationChromePlacement.BOTTOM -> Column(Modifier.fillMaxSize()) {
                Box(contentModifier.weight(1f)) { scene.content() }
                chromeContent()
            }
        }
    }
}

@Composable
private fun ChromeVisibilityLayout(
    visible: Boolean,
    placement: NavigationChromePlacement,
    sharedScope: SharedTransitionScope,
    content: @Composable () -> Unit,
) {
    // Контент остаётся в composition даже при скрытой панели: remember и TV focus requesters не теряются.
    val fraction by animateFloatAsState(if (visible) 1f else 0f, label = "navigation-chrome-size")
    Layout(
        content = content,
        modifier = Modifier.animateBounds(sharedScope).clipToBounds()
            .then(if (visible) Modifier else Modifier.clearAndSetSemantics {}),
    ) { measurables, constraints ->
        val child = measurables.single().measure(constraints.copy(minWidth = 0, minHeight = 0))
        val width = if (placement == NavigationChromePlacement.START) {
            (child.width * fraction).roundToInt()
        } else {
            child.width
        }
        val height = if (placement != NavigationChromePlacement.START) {
            (child.height * fraction).roundToInt()
        } else {
            child.height
        }
        layout(width, height) { if (fraction > 0f) child.placeRelative(0, 0) }
    }
}

private data class ChromeLayout(
    val placement: NavigationChromePlacement,
    val consumedInsets: WindowInsets,
    val sceneContentModifier: Modifier,
    val enabled: Boolean,
)
