package com.quizzgame.shared.catalog.domain.entity

data class QuizUserState(
    val bookmarked: Boolean,
    val progress: Double,
    val status: PassingStatus,
)

enum class PassingStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}
