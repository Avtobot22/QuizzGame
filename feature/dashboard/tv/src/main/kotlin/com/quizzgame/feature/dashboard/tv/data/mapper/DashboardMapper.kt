package com.quizzgame.feature.dashboard.tv.data.mapper

import com.quizzgame.core.media.AppearanceMapper
import com.quizzgame.feature.dashboard.tv.data.dto.DashboardDto
import com.quizzgame.feature.dashboard.tv.data.dto.DashboardTabDto
import com.quizzgame.feature.dashboard.tv.data.dto.DashboardTabIdDto
import com.quizzgame.feature.dashboard.tv.data.dto.NavigationAppearanceDto
import com.quizzgame.feature.dashboard.tv.data.dto.NavigationBackgroundDto
import com.quizzgame.feature.dashboard.tv.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTab
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import com.quizzgame.feature.dashboard.tv.domain.entity.NavigationAppearance
import com.quizzgame.shared.catalog.data.mapper.catalogId
import com.quizzgame.shared.catalog.data.mapper.requireCatalog
import com.quizzgame.shared.catalog.data.mapper.toCatalogBadge
import com.quizzgame.shared.catalog.data.mapper.toCatalogMedia
import javax.inject.Inject

internal class DashboardMapper @Inject constructor() {
    fun map(dto: DashboardDto): Dashboard {
        catalogId(dto.id)
        val ids = dto.tabs.map { it.id }
        requireCatalog(ids.distinct().size == ids.size)
        return Dashboard(dto.tabs.map(::mapTab), mapAppearance(dto.navigationAppearance))
    }

    private fun mapTab(dto: DashboardTabDto): DashboardTab {
        requireCatalog(dto.title.isNotBlank())
        return DashboardTab(
            when (dto.id) {
                DashboardTabIdDto.FEED -> DashboardTabId.FEED
                DashboardTabIdDto.LIBRARY -> DashboardTabId.LIBRARY
                DashboardTabIdDto.SETTINGS -> DashboardTabId.SETTINGS
            },
            dto.title,
            dto.enabled,
            dto.subtitle,
            dto.icon.toCatalogMedia(),
            dto.badge?.toCatalogBadge(),
        )
    }

    private fun mapAppearance(dto: NavigationAppearanceDto?): NavigationAppearance = NavigationAppearance(
        when (dto?.background ?: NavigationBackgroundDto.SOLID) {
            NavigationBackgroundDto.SOLID -> BACKGROUND_SOLID
            NavigationBackgroundDto.TRANSLUCENT -> BACKGROUND_TRANSLUCENT
            NavigationBackgroundDto.TRANSPARENT -> BACKGROUND_TRANSPARENT
        },
        AppearanceMapper.color(dto?.backgroundColor),
        AppearanceMapper.color(dto?.selectedColor),
        AppearanceMapper.color(dto?.unselectedColor),
    )

    private companion object {
        const val BACKGROUND_SOLID = "solid"
        const val BACKGROUND_TRANSLUCENT = "translucent"
        const val BACKGROUND_TRANSPARENT = "transparent"
    }
}
