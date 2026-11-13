package com.quizzgame.mobile

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.mobile.navigation.MobileChromeTags
import com.quizzgame.mobile.navigation.MobileDemoTags
import com.quizzgame.mobile.navigation.MobileNavigationTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MobileShellStateTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val screen: AppShellScreen
        get() = AppShellScreen(compose)

    @Test
    fun joinRoomDemoRetainsLocalInputAcrossActivityRecreation() {
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.JOIN_ROOM).isDisplayed() }
        screen.tag(MobileChromeTags.BAR).assertIsDisplayed()
        screen.tag(MobileDemoTags.ROOM_CODE).performTextInput("abc123")
        compose.activityRule.scenario.recreate()
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.JOIN_ROOM).isDisplayed() }
        screen.tag(MobileDemoTags.ROOM_CODE).assertTextContains("ABC123")
    }

    @Test
    fun keyboardHidesCompactChromeKeepsTheRoomFieldVisibleAndBackRestoresTheBar() {
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.JOIN_ROOM).isDisplayed() }
        screen.tag(MobileDemoTags.ROOM_CODE).performClick()
        compose.waitUntil(5_000) {
            ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        screen.tag(MobileChromeTags.BAR).assertDoesNotExist()
        screen.tag(MobileDemoTags.ROOM_CODE).assertIsDisplayed()
        val bounds = compose.onNodeWithTag(MobileDemoTags.ROOM_CODE).getUnclippedBoundsInRoot()
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val keyboard = checkNotNull(ViewCompat.getRootWindowInsets(view)).getInsets(WindowInsetsCompat.Type.ime())
            with(compose.density) { assertTrue(bounds.bottom.toPx() <= view.height - keyboard.bottom) }
        }
        Espresso.closeSoftKeyboard()
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileChromeTags.BAR).isDisplayed() }
        screen.tag(MobileChromeTags.BAR).assertIsDisplayed()
    }

    @Test
    fun explicitGuestLogoutDiscardsNavigationButRetainsTheAllowedRoomDraft() {
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.JOIN_ROOM).isDisplayed() }
        screen.tag(MobileDemoTags.ROOM_CODE).performTextInput("abc123")
        screen.tag(MobileDemoTags.ROOM_CODE).assertTextContains("ABC123")
        val root = ViewModelProvider(compose.activity)[AppRootViewModel::class.java]
        val previousRevision = root.logoutRevision.value
        screen.tag(MobileDemoTags.action(0)).performClick()
        compose.waitUntil(5_000) { root.logoutRevision.value > previousRevision }
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.JOIN_ROOM).isDisplayed() }
        screen.tag(MobileDemoTags.ROOM_CODE).assertTextContains("ABC123")
    }
}
