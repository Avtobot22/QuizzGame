package com.quizzgame.core.navigation.request

import com.quizzgame.core.navigation.SavedNavKey
import kotlinx.serialization.Serializable

@Serializable
data class NavigationRequest(val id: String, val destination: SavedNavKey, val topLevel: SavedNavKey) {
    init { require(id.isNotBlank()) }
}

fun interface ExternalNavigationParser {
    fun parse(uri: String): NavigationRequest?
}
