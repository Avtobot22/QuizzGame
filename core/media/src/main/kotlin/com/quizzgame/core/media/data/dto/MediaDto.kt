package com.quizzgame.core.media.data.dto

import com.quizzgame.core.media.data.dto.icon.IconVariantDto
import com.quizzgame.core.media.data.dto.icon.IconWeightDto
import com.quizzgame.core.media.data.dto.presentation.MediaLoadingDto
import com.quizzgame.core.media.data.dto.presentation.MediaPresentationDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed interface MediaDto {
    val presentation: MediaPresentationDto?
    val tint: ColorDto?

    @Serializable
    @SerialName("remote")
    data class Remote(
        val url: String,
        override val presentation: MediaPresentationDto? = null,
        override val tint: ColorDto? = null,
        val loading: MediaLoadingDto? = null,
    ) : MediaDto {
        override fun toString(): String = "Remote(<redacted>)"
    }

    @Serializable
    @SerialName("icon")
    data class Icon(
        val name: String,
        val variant: IconVariantDto = IconVariantDto.OUTLINE,
        val weight: IconWeightDto = IconWeightDto.REGULAR,
        override val presentation: MediaPresentationDto? = null,
        override val tint: ColorDto? = null,
    ) : MediaDto

    @Serializable
    @SerialName("asset")
    data class Asset(
        val name: String,
        override val presentation: MediaPresentationDto? = null,
        override val tint: ColorDto? = null,
    ) : MediaDto

    @Serializable
    @SerialName("embedded")
    data class Embedded(
        val mimeType: String,
        val base64: String,
        override val presentation: MediaPresentationDto? = null,
        override val tint: ColorDto? = null,
    ) : MediaDto {
        override fun toString(): String = "Embedded(<redacted>)"
    }
}
