package com.quizzgame.core.media.data.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed interface ColorDto {
    @Serializable
    @SerialName("token")
    data class Token(val name: String) : ColorDto

    @Serializable
    @SerialName("hex")
    data class Hex(val value: String? = null, val light: String? = null, val dark: String? = null) : ColorDto
}
