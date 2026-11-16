package com.quizzgame.feature.dashboard.tv.data.dto

import com.quizzgame.core.media.data.dto.ColorDto
import kotlinx.serialization.Serializable

@Serializable
internal data class NavigationAppearanceDto(
    val background: NavigationBackgroundDto = NavigationBackgroundDto.SOLID,
    val backgroundColor: ColorDto? = null,
    val selectedColor: ColorDto? = null,
    val unselectedColor: ColorDto? = null,
)
