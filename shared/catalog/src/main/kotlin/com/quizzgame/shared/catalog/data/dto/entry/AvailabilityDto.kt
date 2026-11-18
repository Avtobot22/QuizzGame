package com.quizzgame.shared.catalog.data.dto.entry

import kotlinx.serialization.Serializable

@Serializable
data class AvailabilityDto(
    val status: AvailabilityStatusDto,
    val requiresAuthentication: Boolean,
)
