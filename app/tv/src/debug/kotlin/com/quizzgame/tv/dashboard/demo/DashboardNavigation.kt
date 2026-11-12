package com.quizzgame.tv.dashboard.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationState
import com.quizzgame.core.navigation.rememberNavigationState

@Composable
internal fun rememberDashboardNavigation(descriptors: List<DashboardTabDescriptor>): DashboardNavigation {
    val tabs = resolveDashboardTabs(descriptors)
    val roots = tabs.map { DashboardDestination.TabRoot(it.id) }
        .ifEmpty { listOf(DashboardDestination.TabRoot(DashboardTabId.MAIN)) }
    val primary = tabs.firstOrNull { it.id == DashboardTabId.MAIN && it.isEnabled }?.id
        ?: tabs.firstOrNull { it.isEnabled }?.id ?: tabs.firstOrNull()?.id ?: DashboardTabId.MAIN
    val state = key(tabs.isEmpty()) { rememberNavigationState(DashboardDestination.TabRoot(primary), roots) }
    val navigator = remember(state) { AppNavigator(state) }
    val selected = (state.snapshot.active as DashboardDestination.TabRoot).tab
    LaunchedEffect(tabs, selected) {
        val enabled = selectedDashboardTab(selected, tabs)
        if (enabled != null && enabled != selected) navigator.switchTopLevel(DashboardDestination.TabRoot(enabled))
    }
    return DashboardNavigation(tabs, state, navigator)
}

internal class DashboardNavigation(
    val tabs: List<DashboardTab>,
    val state: NavigationState,
    val navigator: AppNavigator,
) {
    val selectedTab: DashboardTabId?
        get() = selectedDashboardTab((state.snapshot.active as DashboardDestination.TabRoot).tab, tabs)
    val canPop: Boolean get() = state.snapshot.activeEntries.size > 1
    val canGoBack: Boolean get() = state.snapshot.canGoBack

    fun select(tab: DashboardTabId) {
        if (tabs.any { it.id == tab && it.isEnabled }) navigator.switchTopLevel(DashboardDestination.TabRoot(tab))
    }

    fun open(destination: androidx.navigation3.runtime.NavKey) { navigator.navigate(destination) }

    fun pop() { navigator.back() }
}
