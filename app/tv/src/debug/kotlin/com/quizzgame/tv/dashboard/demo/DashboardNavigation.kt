package com.quizzgame.tv.dashboard.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.quizzgame.core.navigation.NavAction
import com.quizzgame.core.navigation.dispatch

@Composable
internal fun rememberDashboardNavigation(descriptors: List<DashboardTabDescriptor>): DashboardNavigation {
    val tabs = resolveDashboardTabs(descriptors)
    var savedTab by rememberSaveable { mutableStateOf<DashboardTabId?>(null) }
    val selected = selectedDashboardTab(savedTab, tabs)
    val stacks = tabs.associate { tab ->
        tab.id to key(tab.id) { rememberNavBackStack(DashboardDestination.TabRoot(tab.id)) }
    }
    LaunchedEffect(selected) { savedTab = selected }
    return DashboardNavigation(tabs, selected, stacks) { savedTab = it }
}

internal class DashboardNavigation(
    val tabs: List<DashboardTab>,
    val selectedTab: DashboardTabId?,
    val stacks: Map<DashboardTabId, NavBackStack<NavKey>>,
    private val onSelect: (DashboardTabId) -> Unit,
) {
    val canPop: Boolean get() = selectedTab?.let { stacks.getValue(it).size > 1 } == true

    fun select(tab: DashboardTabId) {
        if (tabs.any { it.id == tab && it.isEnabled }) onSelect(tab)
    }

    fun open(destination: NavKey) {
        selectedTab?.let { stacks.getValue(it).dispatch(NavAction.Push(destination)) }
    }

    fun pop() {
        selectedTab?.let { stacks.getValue(it).dispatch(NavAction.Pop) }
    }
}
