package com.quizzgame.tv.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationState

// Debug Dashboard содержит существующие динамические вкладки и их отдельный host.
@Composable
@Suppress("UnusedParameter")
internal fun TvMainContent(
    state: NavigationState,
    navigator: AppNavigator,
    entries: List<NavEntry<NavKey>>,
    onBack: () -> Unit,
) {
    NavDisplay(entries = entries, onBack = onBack)
}
