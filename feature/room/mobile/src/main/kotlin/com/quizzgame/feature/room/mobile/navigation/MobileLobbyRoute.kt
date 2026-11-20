package com.quizzgame.feature.room.mobile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MobileLobbyRoute(val roomId: String) : NavKey
