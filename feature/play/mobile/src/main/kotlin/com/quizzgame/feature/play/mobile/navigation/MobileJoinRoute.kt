package com.quizzgame.feature.play.mobile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MobileJoinRoute(val code: String, val nickname: String = "") : NavKey
