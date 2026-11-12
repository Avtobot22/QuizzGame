package com.quizzgame.core.navigation

import androidx.annotation.MainThread
import androidx.navigation3.runtime.NavKey

@MainThread
class AppNavigator(val state: NavigationState) {
    fun navigate(destination: NavKey) {
        require(state.snapshot.branches.none { it.root == destination }) { "Use switchTopLevel for section roots" }
        mutateCurrent { entries ->
            val existing = entries.indexOf(destination)
            if (existing >= 0) entries.take(existing + 1) else entries + destination
        }
    }

    fun switchTopLevel(destination: NavKey, onReselected: () -> Unit = {}) {
        val current = state.snapshot
        require(current.branches.any { it.root == destination })
        check(current.flow.isEmpty()) { "Complete the application flow before switching sections" }
        if (current.active == destination) {
            if (current.activeEntries.size > 1) popUpTo(destination) else onReselected()
        } else {
            state.update { it.copy(active = destination) }
        }
    }

    fun replace(destination: NavKey) {
        val current = state.snapshot
        require(
            current.flow.isNotEmpty() || destination == current.active ||
                current.branches.none { it.root == destination },
        )
        if (current.flow.isEmpty() && current.activeEntries.size == 1) {
            require(destination == current.active) { "A section root cannot be replaced" }
        } else {
            mutateCurrent { entries ->
                val index = entries.indexOf(destination)
                if (index >= 0) entries.take(index + 1) else entries.dropLast(1) + destination
            }
        }
    }

    fun replaceRoot(destination: NavKey) {
        state.update { current ->
            val isSection = current.branches.any { it.root == destination }
            current.copy(
                active = if (isSection) destination else current.primary,
                branches = current.branches.map { NavigationBranch(it.root) },
                flow = if (isSection) emptyList() else listOf(destination),
                generation = Math.incrementExact(current.generation),
            )
        }
    }

    fun popUpTo(destination: NavKey, inclusive: Boolean = false) {
        mutateCurrent { entries ->
            val index = entries.indexOf(destination)
            if (index < 0) entries else entries.take((index + if (inclusive) 0 else 1).coerceAtLeast(1))
        }
    }

    fun back(): Boolean {
        val current = state.snapshot
        return when {
            !current.canGoBack -> false
            current.flow.isNotEmpty() || current.activeEntries.size > 1 -> {
                mutateCurrent { it.dropLast(1) }
                true
            }
            else -> {
                state.update { it.copy(active = it.primary) }
                true
            }
        }
    }

    fun showFlow(destination: NavKey) {
        require(state.snapshot.branches.none { it.root == destination })
        state.update { it.copy(flow = listOf(destination)) }
    }

    fun completeFlow() { state.update { it.copy(flow = emptyList()) } }

    private fun mutateCurrent(transform: (List<NavKey>) -> List<NavKey>) {
        state.update { current ->
            if (current.flow.isNotEmpty()) {
                current.copy(flow = transform(current.flow))
            } else {
                current.copy(
                    branches = current.branches.map { branch ->
                        if (branch.root == current.active) branch.copy(entries = transform(branch.entries)) else branch
                    },
                )
            }
        }
    }
}
