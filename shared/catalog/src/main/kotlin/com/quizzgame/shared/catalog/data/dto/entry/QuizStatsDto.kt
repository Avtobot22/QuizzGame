package com.quizzgame.shared.catalog.data.dto.entry

import kotlinx.serialization.Serializable

@Serializable
data class QuizStatsDto(
    val questionCount: Int? = null,
    val estimatedDurationSeconds: Int? = null,
)
