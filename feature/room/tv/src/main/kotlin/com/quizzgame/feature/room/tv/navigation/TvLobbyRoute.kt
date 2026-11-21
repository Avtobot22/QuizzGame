package com.quizzgame.feature.room.tv.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class TvLobbyRoute(val roomId: String) : NavKey
