package com.quizzgame.tv

import android.view.KeyEvent
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class DashboardNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun tabsRetainDetailViewModelAndRecreationStateWhilePopDisposesTheEntry() {
        openDashboard()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.onNodeWithTag("quiz-MAIN-0").assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("quiz-detail")
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.onNodeWithTag("detail-counter").assertTextEquals(compose.activity.getString(R.string.demo_counter, 1))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_UP)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("tab-content-LIBRARY")
        TvRemote.press(KeyEvent.KEYCODE_DPAD_UP)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_LEFT)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("quiz-detail")
        compose.onNodeWithTag("detail-counter").assertTextEquals(compose.activity.getString(R.string.demo_counter, 1))
        compose.activityRule.scenario.recreate()
        awaitVisible("quiz-detail")
        compose.onNodeWithTag("detail-counter").assertTextEquals(compose.activity.getString(R.string.demo_counter, 1))
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        awaitVisible("tab-content-MAIN")
        compose.onNodeWithTag("quiz-MAIN-0").assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("quiz-detail")
        compose.onNodeWithTag("detail-counter").assertTextEquals(compose.activity.getString(R.string.demo_counter, 0))
    }

    @Test
    fun scrollingMainAndPoppingDetailRestoresTheFocusedCard() {
        openDashboard()
        repeat(7) { TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN) }
        compose.onNodeWithTag("quiz-MAIN-6").assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("quiz-detail")
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        awaitVisible("tab-content-MAIN")
        compose.onNodeWithTag("quiz-MAIN-6").assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.onNodeWithTag("tab-MAIN").assertIsFocused()
    }

    private fun openDashboard() {
        awaitVisible("splash")
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("signin")
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("dashboard")
        compose.onNodeWithTag("tab-MAIN").assertIsFocused()
    }

    private fun awaitVisible(tag: String) {
        compose.waitUntil(10_000) { compose.onNodeWithTag(tag).isDisplayed() }
    }
}
