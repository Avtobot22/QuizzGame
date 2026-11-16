package com.quizzgame.feature.feed.tv.data.dto.item

import com.quizzgame.core.media.data.dto.ColorDto
import kotlinx.serialization.Serializable

@Serializable
internal data class QuizCardStyleDto(val titleColor: ColorDto? = null, val subtitleColor: ColorDto? = null)
