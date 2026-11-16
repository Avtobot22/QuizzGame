package com.quizzgame.feature.dashboard.tv.data.dto

import com.quizzgame.core.media.data.dto.MediaDto
import com.quizzgame.shared.catalog.data.dto.BadgeDto
import kotlinx.serialization.Serializable

@Serializable
internal data class DashboardTabDto(
    val id: DashboardTabIdDto,
    val title: String,
    val enabled: Boolean,
    val subtitle: String? = null,
    val icon: MediaDto? = null,
    val badge: BadgeDto? = null,
)
