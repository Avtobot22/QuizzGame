package com.quizzgame.tv.dashboard.demo

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal enum class DashboardTabId(val serverId: String) {
    MAIN("main"),
    LIBRARY("library"),
    SETTINGS("settings"),
}

/** Boundary for a future ordered server configuration, without introducing a Dashboard endpoint. */
internal data class DashboardTabDescriptor(val id: String, val isEnabled: Boolean = true)

internal data class DashboardTab(val id: DashboardTabId, val isEnabled: Boolean = true)

internal fun resolveDashboardTabs(descriptors: List<DashboardTabDescriptor>): List<DashboardTab> =
    descriptors.mapNotNull { descriptor ->
        DashboardTabId.entries.firstOrNull { it.serverId == descriptor.id }
            ?.let { DashboardTab(it, descriptor.isEnabled) }
    }.distinctBy { it.id }

@Serializable
internal sealed interface DashboardDestination : NavKey {
    @Serializable
    data class TabRoot(val tab: DashboardTabId) : DashboardDestination

    @Serializable
    data class QuizDetail(val quizId: String) : DashboardDestination
}

internal fun selectedDashboardTab(selected: DashboardTabId?, tabs: List<DashboardTab>): DashboardTabId? =
    selected?.takeIf { id -> tabs.any { it.id == id && it.isEnabled } }
        ?: tabs.firstOrNull { it.isEnabled }?.id
