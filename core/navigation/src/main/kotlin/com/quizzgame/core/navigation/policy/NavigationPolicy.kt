package com.quizzgame.core.navigation.policy

import androidx.navigation3.runtime.NavKey

enum class DestinationAccess {
    PUBLIC,
    AUTHENTICATED,
    DENIED,
}

fun interface NavigationPolicy {

    fun access(destination: NavKey): DestinationAccess
}
