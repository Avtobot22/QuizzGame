package com.quizzgame.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationState
import com.quizzgame.feature.dashboard.mobile.ui.MobileDashboardShell
import com.quizzgame.feature.play.mobile.navigation.PlayRoute

@Composable
internal fun MobileCatalogNavigationHost(
    state: NavigationState,
    navigator: AppNavigator,
    entries: List<NavEntry<NavKey>>,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
) {
    val parent = checkNotNull(LocalViewModelStoreOwner.current) as HasDefaultViewModelProviderFactory
    val owner = remember(parent) {
        object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory by parent {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        MobileDashboardShell(onSignIn) { dashboard ->
            val items = dashboard.navigationItems()
            val active = state.snapshot.active
            LaunchedEffect(items, active) {
                if (items.none { it.route == active }) navigator.switchTopLevel(PlayRoute)
            }
            MobileMainNavigationHost(state, navigator, entries, onBack, items, dashboard.appearance)
        }
    }
}
