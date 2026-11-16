package com.quizzgame.feature.quizentry.tv.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class QuizEntryRoute(val entryId: String) : NavKey
