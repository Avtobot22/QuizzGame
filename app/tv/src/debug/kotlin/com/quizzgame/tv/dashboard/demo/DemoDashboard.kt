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
import androidx.compose.runtime.saveable.Saver
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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.tv.R
import kotlinx.serialization.json.Json

private val DashboardStateSaver = Saver<DashboardNavState, String>(
    save = { Json.encodeToString(it) },
    restore = { Json.decodeFromString(it) },
)

@Composable
internal fun DemoDashboard(onSignIn: () -> Unit) {
    var state by rememberSaveable(stateSaver = DashboardStateSaver) { mutableStateOf(DashboardNavState()) }
    var contentFocused by rememberSaveable { mutableStateOf(false) }
    val tabFocus = remember { DashboardTabId.entries.associateWith { FocusRequester() } }
    val entries = dashboardEntries(
        state,
        requestFocus = contentFocused,
        open = { state = state.open(it) },
        back = { state = state.pop() },
    )
    Column(Modifier.fillMaxSize().padding(48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.demo_label))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            state.availableTabs.forEach { tab ->
                QuizzTvButton(
                    onClick = {
                        if (state.selectedTab != tab) {
                            state = state.select(tab)
                            contentFocused = true
                        }
                    },
                    modifier = Modifier.testTag("tab-$tab").focusRequester(tabFocus.getValue(tab))
                        .onFocusChanged { if (it.isFocused) contentFocused = false }
                        .semantics { selected = state.selectedTab == tab },
                ) {
                    Text(
                        stringResource(tab.titleResource),
                        fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
            QuizzTvButton(onSignIn, Modifier.testTag("dashboard-signin")) {
                Text(stringResource(R.string.demo_signout))
            }
        }
        val selected = state.selectedTab
        if (selected != null) {
            Box(Modifier.weight(1f).onFocusChanged { if (it.hasFocus) contentFocused = true }.focusGroup()) {
                NavDisplay(entries = entries.getValue(selected), onBack = { state = state.pop() })
            }
        }
    }
    LaunchedEffect(Unit) {
        if (!contentFocused) state.selectedTab?.let { tabFocus.getValue(it).requestFocus() }
    }
    BackHandler(enabled = state.canPop || contentFocused) {
        if (state.canPop) state = state.pop() else state.selectedTab?.let { tabFocus.getValue(it).requestFocus() }
    }
}

@Composable
private fun dashboardEntries(
    state: DashboardNavState,
    requestFocus: Boolean,
    open: (DashboardDestination) -> Unit,
    back: () -> Unit,
): Map<DashboardTabId, List<NavEntry<DashboardDestination>>> {
    val provider = entryProvider<DashboardDestination> {
        entry<DashboardDestination.TabRoot> { route ->
            DemoTabContent(route.tab, requestFocus, open)
        }
        entry<DashboardDestination.QuizDetail> { route ->
            DemoQuizDetail(route.quizId, back)
        }
    }
    // All three decorators stay composed even when only one tab is displayed.
    return DashboardTabId.entries.associateWith { tab ->
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
