package com.quizzgame.tv.navigation

import androidx.navigation3.runtime.NavKey

internal data class DashboardNavigationContext(
    val requestFocus: Boolean,
    val open: (NavKey) -> Unit,
    val back: () -> Unit,
)
