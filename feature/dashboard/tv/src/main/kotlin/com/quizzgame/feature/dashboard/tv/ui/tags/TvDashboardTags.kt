package com.quizzgame.feature.dashboard.tv.ui.tags

import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId

object TvDashboardTags {
    const val NAVIGATION = "dashboard:navigation"
    const val SIGN_OUT = "dashboard:sign-out"
    fun tab(id: DashboardTabId): String = "dashboard:tab:${id.name.lowercase()}"
    const val CONTENT = "dashboard:content"
    const val LOADING = "dashboard:loading"
    const val EMPTY = "dashboard:empty"
    const val RETRY = "dashboard:retry"
}
