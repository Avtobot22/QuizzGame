package com.quizzgame.feature.dashboard.tv.domain.entity

import com.quizzgame.core.media.ColorSpec
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.shared.catalog.domain.entity.CatalogBadge

internal data class Dashboard(val tabs: List<DashboardTab>, val appearance: NavigationAppearance)

internal data class DashboardTab(
    val id: DashboardTabId,
    val title: String,
    val enabled: Boolean,
    val subtitle: String?,
    val icon: MediaSpec,
    val badge: CatalogBadge?,
)

internal data class NavigationAppearance(
    val background: String,
    val backgroundColor: ColorSpec?,
    val selectedColor: ColorSpec?,
    val unselectedColor: ColorSpec?,
)
