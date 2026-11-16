package com.quizzgame.feature.quizentry.tv.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PrimaryActionDto(
    val kind: PrimaryActionKindDto,
    val title: String,
    val enabled: Boolean,
)
