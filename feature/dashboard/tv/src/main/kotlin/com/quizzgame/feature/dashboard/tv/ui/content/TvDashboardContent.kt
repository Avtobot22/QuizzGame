package com.quizzgame.feature.dashboard.tv.ui.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.chrome.MainNavigationHost
import com.quizzgame.core.navigation.chrome.NavigationChromePlacement
import com.quizzgame.core.navigation.decoratedEntries
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.registration.rememberFeatureEntryProvider
import com.quizzgame.core.navigation.rememberNavigationState
import com.quizzgame.feature.dashboard.tv.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.tv.navigation.DashboardContext
import com.quizzgame.feature.dashboard.tv.navigation.DashboardTabRoute
import com.quizzgame.feature.dashboard.tv.ui.navigation.TvDashboardBar

@Composable
internal fun TvDashboardContent(
    dashboard: Dashboard,
    graphs: Set<FeatureEntries<DashboardContext>>,
    onProfile: () -> Unit,
) {
    val primary = dashboard.tabs.first { it.enabled }
    val roots = dashboard.tabs.map { DashboardTabRoute(it.id) }
    val state = rememberNavigationState(DashboardTabRoute(primary.id), roots)
    val navigator = remember(state) { AppNavigator(state) }
    val selected = state.snapshot.active as DashboardTabRoute
    var contentFocused by rememberSaveable { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(dashboard.tabs, selected) {
        if (dashboard.tabs.none { it.id == selected.id && it.enabled }) {
            navigator.switchTopLevel(DashboardTabRoute(primary.id))
        }
    }
    val provider = rememberFeatureEntryProvider(
        graphs,
        DashboardContext(
            contentFocused,
            open = {
                navigator.navigate(it)
                true
            },
            back = { navigator.back() },
        ),
    )
    MainNavigationHost(
        entries = state.decoratedEntries(provider),
        placement = NavigationChromePlacement.TOP,
        onBack = { navigator.back() },
        consumedInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        sceneContentModifier = Modifier.onFocusChanged { if (it.hasFocus) contentFocused = true }.focusGroup(),
    ) { visible ->
        TvDashboardBar(
            dashboard,
            selected.id,
            focus,
            visible && !contentFocused,
            onProfile,
            onSelect = {
                navigator.switchTopLevel(DashboardTabRoute(it))
                contentFocused = true
            },
            onBarFocus = { contentFocused = false },
        )
    }
    BackHandler(enabled = state.snapshot.activeEntries.size > 1 || contentFocused) {
        if (state.snapshot.activeEntries.size > 1) navigator.back() else contentFocused = false
    }
}
