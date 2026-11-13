package com.quizzgame.app.navigation

import com.quizzgame.core.navigation.request.NavigationRequest
import kotlinx.serialization.Serializable

@Serializable
data class NavigationCheckpoint(
    val owner: String? = null,
    val validatedIdentity: String? = null,
    val admitted: Boolean = false,
    val pending: NavigationRequest? = null,
    val handledRequestId: String? = null,
)
