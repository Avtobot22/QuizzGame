package com.quizzgame.tv.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvNavigationBar
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationState
import com.quizzgame.core.navigation.chrome.MainNavigationHost
import com.quizzgame.core.navigation.chrome.NavigationChromePlacement
import com.quizzgame.tv.R

@Composable
internal fun TvMainContent(
    state: NavigationState,
    navigator: AppNavigator,
    entries: List<NavEntry<NavKey>>,
    onBack: () -> Unit,
) {
    val roots = state.snapshot.branches.map { it.root }
    val focus = remember { FocusRequester() }
    var attached by remember { mutableStateOf(false) }
    MainNavigationHost(
        entries,
        NavigationChromePlacement.TOP,
        onBack,
        consumedInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
    ) { visible ->
        QuizzTvNavigationBar(
            items = roots,
            selected = state.snapshot.active,
            onSelect = { if (visible) navigator.switchTopLevel(it) },
            label = { Text(stringResource(R.string.navigation_dashboard)) },
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 24.dp).testTag(TvNavigationTags.NAVIGATION_BAR),
            itemModifier = { Modifier.focusRequester(focus).onGloballyPositioned { attached = true } },
        )
        TvNavigationFocus(focus, attached, visible)
    }
}
