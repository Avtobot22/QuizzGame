package com.quizzgame.feature.dashboard.mobile.data.dto

import com.quizzgame.core.media.data.dto.MediaDto
import com.quizzgame.shared.catalog.data.dto.BadgeDto
import kotlinx.serialization.Serializable

@Serializable
internal data class DashboardTabDto(
    val id: String,
    val title: String,
    val enabled: Boolean,
    val icon: MediaDto? = null,
    val badge: BadgeDto? = null,
)
