package com.quizzgame.feature.dashboard.tv.navigation

import androidx.navigation3.runtime.NavKey
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import kotlinx.serialization.Serializable

@Serializable
data class DashboardTabRoute(val id: DashboardTabId) : NavKey

data class DashboardContext(val requestFocus: Boolean, val open: (NavKey) -> Boolean, val back: () -> Unit)
