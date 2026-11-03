package com.quizzgame.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface RootNavKey : NavKey

@Serializable
data object SplashRoute : RootNavKey

@Serializable
data object SignInRoute : RootNavKey
