package com.quizzgame.core.navigation

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.dispatch(action: NavAction) {
    val next = reduceNavigation(toList(), action)
    if (next == this) return
    Snapshot.withMutableSnapshot {
        clear()
        addAll(next)
    }
}
