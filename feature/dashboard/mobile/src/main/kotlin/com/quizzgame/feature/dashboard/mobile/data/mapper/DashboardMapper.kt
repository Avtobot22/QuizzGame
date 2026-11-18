package com.quizzgame.feature.dashboard.mobile.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.feature.dashboard.mobile.data.dto.DashboardDto
import com.quizzgame.feature.dashboard.mobile.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.mobile.domain.entity.DashboardTab
import com.quizzgame.feature.dashboard.mobile.domain.entity.MobileTabId
import com.quizzgame.feature.dashboard.mobile.domain.entity.NavigationAppearance
import com.quizzgame.shared.catalog.data.mapper.catalogId
import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import com.quizzgame.shared.catalog.data.mapper.toCatalogBadge
import com.quizzgame.shared.catalog.data.mapper.toCatalogMedia
import javax.inject.Inject

internal class DashboardMapper @Inject constructor() {
    fun map(dto: DashboardDto): Dashboard {
        catalogId(dto.id)
        val known = dto.tabs.mapNotNull { tab ->
            MobileTabId.entries.firstOrNull { it.name.lowercase() == tab.id }?.let { it to tab }
        }
        requireCatalog(known.map { it.first }.distinct().size == known.size)
        requireCatalog(known.any { it.first == MobileTabId.PLAY && it.second.enabled })
        val tabs = known.mapNotNull { (id, tab) ->
            requireCatalog(tab.title.isNotBlank())
            if (tab.enabled) {
                DashboardTab(
                    id,
                    tab.title,
                    tab.icon.toCatalogMedia(),
                    tab.badge?.toCatalogBadge(),
                )
            } else {
                null
            }
        }
        val style = dto.navigationAppearance
        requireCatalog(style == null || style.background in setOf("solid", "translucent", "transparent"))
        return Dashboard(
            tabs,
            NavigationAppearance(
                style?.background ?: "solid",
                AppearanceMapper.color(style?.backgroundColor),
                AppearanceMapper.color(style?.selectedColor),
                AppearanceMapper.color(style?.unselectedColor),
            ),
        )
    }
}
