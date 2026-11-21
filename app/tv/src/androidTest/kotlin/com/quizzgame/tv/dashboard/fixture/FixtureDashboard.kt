package com.quizzgame.tv.dashboard.fixture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvNavigationBar
import com.quizzgame.core.navigation.chrome.MainNavigationHost
import com.quizzgame.core.navigation.chrome.NavigationChromePlacement
import com.quizzgame.core.navigation.decoratedEntries
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.registration.rememberFeatureEntryProvider
import com.quizzgame.tv.dashboard.fixture.ui.tags.FixtureDashboardTags
import com.quizzgame.tv.navigation.DashboardNavigationContext
import com.quizzgame.tv.navigation.fixtureString
import com.quizzgame.tv.ui.tags.TvNavigationTags
import com.quizzgame.tv.test.R as FixtureR

private val FixtureDashboardTabs = DashboardTabId.entries.map { DashboardTabDescriptor(it.serverId) }

@Composable
internal fun FixtureDashboard(
    onLogout: () -> Unit,
    graphs: Set<FeatureEntries<DashboardNavigationContext>>,
    tabs: List<DashboardTabDescriptor> = FixtureDashboardTabs,
) {
    val current = rememberDashboardNavigation(tabs)
    var contentFocused by rememberSaveable { mutableStateOf(false) }
    val tabFocus = remember { DashboardTabId.entries.associateWith { FocusRequester() } }
    val provider = rememberFeatureEntryProvider(
        graphs,
        DashboardNavigationContext(contentFocused, current::open, current::pop),
    )
    val entries = current.state.decoratedEntries(provider)
    if (current.selectedTab == null) {
        Column(Modifier.fillMaxSize().padding(48.dp)) {
            Text(fixtureString(FixtureR.string.fixture_dashboard_empty), Modifier.testTag(FixtureDashboardTags.EMPTY))
            QuizzTvButton(onLogout) { Text(fixtureString(FixtureR.string.fixture_signout)) }
        }
        return
    }
    MainNavigationHost(
        entries = entries,
        placement = NavigationChromePlacement.TOP,
        onBack = { current.pop() },
        consumedInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        sceneContentModifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
            .onFocusChanged { if (it.hasFocus) contentFocused = true }.focusGroup(),
    ) { visible ->
        DashboardNavigationBar(current, contentFocused, visible, tabFocus, onLogout) { contentFocused = it }
    }
    BackHandler(enabled = current.selectedTab != null && (current.canGoBack || contentFocused)) {
        when {
            current.canPop -> current.pop()
            contentFocused -> current.selectedTab?.let { tabFocus.getValue(it).requestFocus() }
            else -> current.pop()
        }
    }
}

@Composable
private fun DashboardNavigationBar(
    current: DashboardNavigation,
    contentFocused: Boolean,
    visible: Boolean,
    tabFocus: Map<DashboardTabId, FocusRequester>,
    onLogout: () -> Unit,
    onContentFocused: (Boolean) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(visible) {
        if (!visible && !contentFocused) {
            focusManager.clearFocus()
            onContentFocused(true)
        }
    }
    Column(Modifier.padding(start = 48.dp, end = 48.dp, top = 48.dp, bottom = 16.dp)) {
        Text(fixtureString(FixtureR.string.fixture_label))
        QuizzTvNavigationBar(
            items = current.tabs.map { it.id },
            selected = current.selectedTab,
            onSelect = {
                current.select(it)
                onContentFocused(true)
            },
            enabled = { id -> current.tabs.any { it.id == id && it.isEnabled } },
            modifier = Modifier.testTag(TvNavigationTags.NAVIGATION_BAR),
            itemModifier = { tab ->
                Modifier.testTag(FixtureDashboardTags.tab(tab)).focusRequester(tabFocus.getValue(tab))
                    .onFocusChanged { if (it.isFocused) onContentFocused(false) }
            },
            label = { tab ->
                Text(
                    fixtureString(tab.titleResource),
                    fontWeight = if (current.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                )
            },
            trailingContent = {
                QuizzTvButton(onLogout, Modifier.testTag(FixtureDashboardTags.SIGN_OUT)) {
                    Text(fixtureString(FixtureR.string.fixture_signout))
                }
            },
        )
    }
    current.selectedTab?.let { tab ->
        TvEntryFocus(
            tabFocus.getValue(tab),
            visible && !contentFocused,
        )
    }
}

internal val DashboardTabId.titleResource: Int
    get() = when (this) {
        DashboardTabId.MAIN -> FixtureR.string.fixture_tab_main
        DashboardTabId.LIBRARY -> FixtureR.string.fixture_tab_library
        DashboardTabId.SETTINGS -> FixtureR.string.fixture_tab_settings
    }
