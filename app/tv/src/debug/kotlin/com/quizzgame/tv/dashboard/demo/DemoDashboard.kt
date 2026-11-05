package com.quizzgame.tv.dashboard.demo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.registration.rememberFeatureEntryProvider
import com.quizzgame.tv.R
import com.quizzgame.tv.navigation.DashboardNavigationContext

private val DemoDashboardTabs = DashboardTabId.entries.map { DashboardTabDescriptor(it.serverId) }

@Composable
internal fun DemoDashboard(
    onLogout: () -> Unit,
    graphs: Set<FeatureEntries<DashboardNavigationContext>>,
    tabs: List<DashboardTabDescriptor> = DemoDashboardTabs,
) {
    val current = rememberDashboardNavigation(tabs)
    var contentFocused by rememberSaveable { mutableStateOf(false) }
    val tabFocus = remember { DashboardTabId.entries.associateWith { FocusRequester() } }
    val entries = dashboardEntries(
        current,
        requestFocus = contentFocused,
        graphs = graphs,
        open = current::open,
        back = current::pop,
    )
    Column(Modifier.fillMaxSize().padding(48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.demo_label))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            current.tabs.forEach { descriptor ->
                val tab = descriptor.id
                QuizzTvButton(
                    onClick = {
                        if (current.selectedTab != tab) {
                            current.select(tab)
                            contentFocused = true
                        }
                    },
                    enabled = descriptor.isEnabled,
                    modifier = Modifier.testTag("tab-$tab").focusRequester(tabFocus.getValue(tab))
                        .onFocusChanged { if (it.isFocused) contentFocused = false }
                        .semantics { selected = current.selectedTab == tab },
                ) {
                    Text(
                        stringResource(tab.titleResource),
                        fontWeight = if (current.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
            QuizzTvButton(onLogout, Modifier.testTag("dashboard-signin")) {
                Text(stringResource(R.string.demo_signout))
            }
        }
        val selected = current.selectedTab
        if (selected != null) {
            Box(Modifier.weight(1f).onFocusChanged { if (it.hasFocus) contentFocused = true }.focusGroup()) {
                NavDisplay(entries = entries.getValue(selected), onBack = { current.pop() })
            }
        } else {
            Text(stringResource(R.string.demo_dashboard_empty), Modifier.testTag("dashboard-empty"))
        }
    }
    LaunchedEffect(current.selectedTab, current.tabs) {
        if (!contentFocused) current.selectedTab?.let { tabFocus.getValue(it).requestFocus() }
    }
    BackHandler(enabled = current.selectedTab != null && (current.canPop || contentFocused)) {
        if (current.canPop) current.pop() else current.selectedTab?.let { tabFocus.getValue(it).requestFocus() }
    }
}

@Composable
private fun dashboardEntries(
    state: DashboardNavigation,
    graphs: Set<FeatureEntries<DashboardNavigationContext>>,
    requestFocus: Boolean,
    open: (NavKey) -> Unit,
    back: () -> Unit,
): Map<DashboardTabId, List<NavEntry<NavKey>>> {
    val provider = rememberFeatureEntryProvider(graphs, DashboardNavigationContext(requestFocus, open, back))
    // Existing tabs retain their decorators; a removed tab disposes its entire private subtree.
    return state.tabs.map { it.id }.associateWith { tab ->
        key(tab) {
            rememberDecoratedNavEntries(
                backStack = state.stacks.getValue(tab),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = provider,
            )
        }
    }
}

internal val DashboardTabId.titleResource: Int
    get() = when (this) {
        DashboardTabId.MAIN -> R.string.demo_tab_main
        DashboardTabId.LIBRARY -> R.string.demo_tab_library
        DashboardTabId.SETTINGS -> R.string.demo_tab_settings
    }
