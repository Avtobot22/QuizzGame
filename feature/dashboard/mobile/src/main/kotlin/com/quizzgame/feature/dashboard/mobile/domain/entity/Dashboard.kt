package com.quizzgame.feature.dashboard.mobile.domain.entity

import com.quizzgame.core.media.ColorSpec
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge

data class Dashboard(val tabs: List<DashboardTab>, val appearance: NavigationAppearance = NavigationAppearance())
data class DashboardTab(
    val id: MobileTabId,
    val title: String? = null,
    val icon: MediaSpec = MediaSpec.Placeholder,
    val badge: CatalogBadge? = null,
)
data class NavigationAppearance(
    val background: String = "solid",
    val backgroundColor: ColorSpec? = null,
    val selectedColor: ColorSpec? = null,
    val unselectedColor: ColorSpec? = null,
)

internal fun localDashboard() = Dashboard(MobileTabId.entries.map { DashboardTab(it) })
