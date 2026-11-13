package com.quizzgame.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavKeySerializer
import kotlinx.serialization.Serializable

typealias SavedNavKey =
    @Serializable(with = NavKeySerializer::class)
    NavKey

@Serializable
data class NavigationBranch(val root: SavedNavKey, val entries: List<SavedNavKey> = listOf(root)) {
    init {
        require(entries.isNotEmpty() && entries.first() == root)
        require(entries.distinct().size == entries.size) { "A destination has one entry per stack" }
    }
}

@Serializable
data class NavigationSnapshot(
    val primary: SavedNavKey,
    val active: SavedNavKey = primary,
    val branches: List<NavigationBranch> = listOf(NavigationBranch(primary)),
    val flow: List<SavedNavKey> = emptyList(),
    val generation: Long = 0,
) {
    init {
        require(branches.map { it.root }.distinct().size == branches.size)
        require(branches.any { it.root == primary } && branches.any { it.root == active })
        require(flow.distinct().size == flow.size)
    }

    val activeEntries: List<NavKey> get() = branches.first { it.root == active }.entries
    val visibleEntries: List<NavKey> get() = flow.ifEmpty { activeEntries }
    val canGoBack: Boolean get() = if (flow.isNotEmpty()) flow.size > 1 else activeEntries.size > 1 || active != primary
}
