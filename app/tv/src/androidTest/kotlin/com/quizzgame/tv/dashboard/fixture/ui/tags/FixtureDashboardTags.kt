package com.quizzgame.tv.dashboard.fixture.ui.tags

import com.quizzgame.tv.dashboard.fixture.DashboardTabId

internal object FixtureDashboardTags {
    const val SIGN_OUT = "dashboard-signin"
    const val EMPTY = "dashboard-empty"
    const val DETAIL = "quiz-detail"
    const val COUNTER = "detail-counter"
    const val INCREMENT = "detail-increment"
    const val BACK = "detail-back"

    fun tab(tab: DashboardTabId): String = "tab-$tab"

    fun tabContent(tab: DashboardTabId): String = "tab-content-$tab"

    fun quiz(tab: DashboardTabId, id: Int): String = "quiz-$tab-$id"
}
