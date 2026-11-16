package com.quizzgame.core.navigation.chrome.ui.tags

internal object NavigationChromeTestTags {
    const val CHROME = "chrome"

    fun route(route: Enum<*>): String = route.name
    fun value(route: Enum<*>): String = "value-$route"
    fun increment(route: Enum<*>): String = "increment-$route"
}
