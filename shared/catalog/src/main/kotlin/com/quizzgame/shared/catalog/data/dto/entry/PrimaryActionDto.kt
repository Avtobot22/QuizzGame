package com.quizzgame.shared.catalog.data.dto.entry

import kotlinx.serialization.Serializable

@Serializable
data class PrimaryActionDto(
    val kind: PrimaryActionKindDto,
    val title: String,
    val enabled: Boolean,
)
