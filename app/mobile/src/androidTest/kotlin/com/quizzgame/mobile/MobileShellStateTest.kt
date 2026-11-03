package com.quizzgame.mobile

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class MobileShellStateTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun joinRoomDemoRetainsLocalInputAcrossActivityRecreation() {
        compose.waitUntil(10_000) { compose.onNodeWithTag("splash").isDisplayed() }
        compose.onNodeWithTag("demo-action-0").performClick()
        compose.waitUntil(5_000) { compose.onNodeWithTag("signin").isDisplayed() }
        compose.onNodeWithTag("demo-action-0").performClick()
        compose.waitUntil(5_000) { compose.onNodeWithTag("join-room").isDisplayed() }
        compose.onNodeWithTag("demo-room-code").performTextInput("abc123")
        compose.activityRule.scenario.recreate()
        compose.waitUntil(5_000) { compose.onNodeWithTag("join-room").isDisplayed() }
        compose.onNodeWithTag("demo-room-code").assertTextContains("ABC123")
    }
}
