package com.quizzgame.tv.dashboard.demo

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal enum class DashboardTabId { MAIN, LIBRARY, SETTINGS }

@Serializable
internal sealed interface DashboardDestination : NavKey {
    @Serializable
    data class TabRoot(val tab: DashboardTabId) : DashboardDestination

    @Serializable
    data class QuizDetail(val quizId: String) : DashboardDestination
}

/** Temporary local Dashboard policy. Server tabs and feature data do not belong here. */
@Serializable
internal data class DashboardNavState(
    val selectedTab: DashboardTabId? = DashboardTabId.MAIN,
    val availableTabs: List<DashboardTabId> = DashboardTabId.entries,
    val stacks: Map<DashboardTabId, List<DashboardDestination>> = DashboardTabId.entries.associateWith {
        listOf(DashboardDestination.TabRoot(it))
    },
) {
    val canPop: Boolean get() = selectedTab?.let { stacks.getValue(it).size > 1 } == true

    fun select(tab: DashboardTabId): DashboardNavState =
        if (tab in availableTabs) copy(selectedTab = tab) else this

    fun open(destination: DashboardDestination): DashboardNavState {
        val tab = selectedTab ?: return this
        val stack = stacks.getValue(tab)
        if (stack.last() == destination) return this
        return copy(stacks = stacks + (tab to stack + destination))
    }

    fun pop(): DashboardNavState {
        val tab = selectedTab ?: return this
        if (!canPop) return this
        return copy(stacks = stacks + (tab to stacks.getValue(tab).dropLast(1)))
    }

    fun reconfigure(tabs: List<DashboardTabId>): DashboardNavState {
        val available = tabs.distinct()
        return copy(
            availableTabs = available,
            selectedTab = selectedTab?.takeIf { it in available } ?: available.firstOrNull(),
            stacks = stacks.mapValues { (tab, stack) ->
                if (tab in available) stack else listOf(DashboardDestination.TabRoot(tab))
            },
        )
    }
}
