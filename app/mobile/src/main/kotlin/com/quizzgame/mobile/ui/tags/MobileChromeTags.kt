package com.quizzgame.mobile.ui.tags

import androidx.navigation3.runtime.NavKey

internal object MobileChromeTags {
    const val BAR = "mobile-navigation-bar"
    const val RAIL = "mobile-navigation-rail"
    fun item(route: NavKey): String = "navigation-item-$route"
}
