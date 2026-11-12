package com.quizzgame.core.navigation.chrome

import androidx.navigation3.runtime.NavEntry

/** DEFAULT показывает панель, если сцена содержит корень раздела. HIDDEN имеет приоритет в multi-pane сцене. */
enum class NavigationChromePolicy { DEFAULT, VISIBLE, HIDDEN }

private const val CHROME_POLICY = "com.quizzgame.navigation.chrome"

fun navigationChrome(policy: NavigationChromePolicy): Map<String, Any> = mapOf(CHROME_POLICY to policy)

internal const val TOP_LEVEL_ROOT = "com.quizzgame.navigation.top-level-root"

internal fun chromeVisible(entries: List<NavEntry<*>>): Boolean {
    val policies = entries.map {
        it.metadata[CHROME_POLICY] as? NavigationChromePolicy ?: NavigationChromePolicy.DEFAULT
    }
    return when {
        NavigationChromePolicy.HIDDEN in policies -> false
        NavigationChromePolicy.VISIBLE in policies -> true
        else -> entries.any { it.metadata[TOP_LEVEL_ROOT] == true }
    }
}
