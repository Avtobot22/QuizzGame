package com.quizzgame.mobile

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.ViewModelProvider
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.mobile.navigation.MobileDemoTags
import com.quizzgame.mobile.navigation.MobileNavigationTags
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
        screen.tag(MobileDemoTags.ROOM_CODE).performTextInput("abc123")
        compose.activityRule.scenario.recreate()
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.JOIN_ROOM).isDisplayed() }
        screen.tag(MobileDemoTags.ROOM_CODE).assertTextContains("ABC123")
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
