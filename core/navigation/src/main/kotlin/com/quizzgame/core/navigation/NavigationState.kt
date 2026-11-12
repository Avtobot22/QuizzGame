package com.quizzgame.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavKey

class NavigationState internal constructor(private val storage: MutableState<NavigationSnapshot>) {
    constructor(initial: NavigationSnapshot) : this(mutableStateOf(initial))

    val snapshot: NavigationSnapshot get() = storage.value

    internal fun update(transform: (NavigationSnapshot) -> NavigationSnapshot) {
        storage.value = transform(storage.value)
    }

    internal fun configure(primary: NavKey, roots: List<NavKey>) {
        update { current ->
            current.copy(
                primary = primary,
                active = current.active.takeIf { it in roots } ?: primary,
                branches = roots.map { root ->
                    current.branches.find { it.root == root } ?: NavigationBranch(root)
                },
            )
        }
    }
}

@Composable
fun rememberNavigationState(
    primary: NavKey,
    topLevels: List<NavKey> = listOf(primary),
    initialFlow: List<NavKey> = emptyList(),
): NavigationState {
    require(primary in topLevels && topLevels.distinct().size == topLevels.size)
    val storage = rememberSerializable(stateSerializer = NavigationSnapshot.serializer()) {
        mutableStateOf(NavigationSnapshot(primary, branches = topLevels.map(::NavigationBranch), flow = initialFlow))
    }
    return remember(storage, primary, topLevels) {
        NavigationState(storage).apply { configure(primary, topLevels) }
    }
}
