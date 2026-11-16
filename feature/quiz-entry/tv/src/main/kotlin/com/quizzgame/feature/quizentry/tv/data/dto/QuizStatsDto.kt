package com.quizzgame.feature.quizentry.tv.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class QuizStatsDto(
    val questionCount: Int? = null,
    val estimatedDurationSeconds: Int? = null,
)
