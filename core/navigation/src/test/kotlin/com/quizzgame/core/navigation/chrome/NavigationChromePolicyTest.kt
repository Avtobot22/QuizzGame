package com.quizzgame.core.navigation.chrome

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationChromePolicyTest {
    private val roots = setOf<NavKey>(Key.HOME, Key.LIBRARY)

    @Test
    fun defaultFollowsTheEntriesDisplayedByTheSceneInsteadOfTheLastBackStackKey() {
        assertTrue(chromeVisible(listOf(entry(Key.HOME))))
        assertFalse(chromeVisible(listOf(entry(Key.DETAIL))))
        assertTrue(chromeVisible(listOf(entry(Key.HOME), entry(Key.DETAIL))))
    }

    @Test
    fun explicitVisibilityKeepsChromeForNestedScenesAndHiddenWinsForFullscreenPanes() {
        assertTrue(chromeVisible(listOf(entry(Key.DETAIL, NavigationChromePolicy.VISIBLE))))
        assertFalse(chromeVisible(listOf(entry(Key.HOME, NavigationChromePolicy.HIDDEN))))
        assertFalse(
            chromeVisible(
                listOf(
                    entry(Key.HOME, NavigationChromePolicy.VISIBLE),
                    entry(Key.DETAIL, NavigationChromePolicy.HIDDEN),
                ),
            ),
        )
    }

    @Test
    fun emptyScenesNeverGainChromeByDefault() { assertFalse(chromeVisible(emptyList())) }

    private fun entry(key: Key, policy: NavigationChromePolicy = NavigationChromePolicy.DEFAULT): NavEntry<NavKey> =
        NavEntry(key, metadata = navigationChrome(policy) + (TOP_LEVEL_ROOT to (key in roots))) { }

    private enum class Key : NavKey { HOME, LIBRARY, DETAIL }
}
