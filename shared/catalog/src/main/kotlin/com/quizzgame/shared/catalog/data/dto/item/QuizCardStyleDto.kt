package com.quizzgame.shared.catalog.data.dto.item

import com.quizzgame.core.media.data.dto.ColorDto
import kotlinx.serialization.Serializable

@Serializable
data class QuizCardStyleDto(val titleColor: ColorDto? = null, val subtitleColor: ColorDto? = null)
