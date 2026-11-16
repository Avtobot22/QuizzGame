package com.quizzgame.feature.quizentry.tv.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class AvailabilityDto(
    val status: AvailabilityStatusDto,
    val requiresAuthentication: Boolean,
)
