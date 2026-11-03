package com.quizzgame.core.navigation

import androidx.navigation3.runtime.NavKey

sealed interface NavAction {
    data class Push(val destination: NavKey) : NavAction
    data object Pop : NavAction
    data class ResetTo(val destination: NavKey) : NavAction
}

fun reduceNavigation(stack: List<NavKey>, action: NavAction): List<NavKey> {
    require(stack.isNotEmpty()) { "A navigation stack must have a root" }
    return when (action) {
        is NavAction.Push -> if (stack.last() == action.destination) stack else stack + action.destination
        NavAction.Pop -> if (stack.size > 1) stack.dropLast(1) else stack
        is NavAction.ResetTo -> listOf(action.destination)
    }
}

fun mayRestoreNavigation(savedProcessId: String?, currentProcessId: String): Boolean =
    savedProcessId != null && savedProcessId == currentProcessId
