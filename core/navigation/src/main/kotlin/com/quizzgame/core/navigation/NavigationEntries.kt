package com.quizzgame.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

@Composable
fun NavigationState.decoratedEntries(provider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
    val current = snapshot
    return key(current.generation) {
        val branches = current.branches.associate { branch ->
            branch.root to key(branch.root) {
                decoratedStack(branch.entries, "section:${branch.root}:${branch.root::class.java.name}", provider)
            }
        }
        val flow = key("application-flow") { decoratedStack(current.flow, "flow", provider) }
        if (current.flow.isNotEmpty()) flow else branches.getValue(current.active)
    }
}

@Composable
private fun decoratedStack(
    entries: List<NavKey>,
    scope: String,
    provider: (NavKey) -> NavEntry<NavKey>,
): List<NavEntry<NavKey>> =
    rememberDecoratedNavEntries(
        backStack = entries,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { route ->
            val entry = provider(route)
            NavEntry(route, contentKey = "$scope:${entry.contentKey}", metadata = entry.metadata) { entry.Content() }
        },
    )
