package com.quizzgame.shared.catalog.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserQuizStateDto(
    val bookmarked: Boolean,
    val progress: Double,
    val passingStatus: PassingStatusDto,
)
