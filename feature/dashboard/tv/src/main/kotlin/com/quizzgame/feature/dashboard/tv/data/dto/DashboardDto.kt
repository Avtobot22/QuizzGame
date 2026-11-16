package com.quizzgame.feature.dashboard.tv.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class DashboardDto(
    val id: String,
    val tabs: List<DashboardTabDto>,
    val navigationAppearance: NavigationAppearanceDto? = null,
)
