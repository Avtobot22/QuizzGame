package com.quizzgame.mobile

internal object MobileNavigationChromeTestTags {
    const val SHEET_CONTENT = "sheet-content"

    fun route(route: Enum<*>): String = route.name
}
