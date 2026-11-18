package com.quizzgame.feature.quizentry.mobile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MobileQuizEntryRoute(val entryId: String) : NavKey
