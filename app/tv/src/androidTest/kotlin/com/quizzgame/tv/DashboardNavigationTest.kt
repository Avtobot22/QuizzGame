package com.quizzgame.tv

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.quizzgame.tv.dashboard.demo.DashboardTabId
import com.quizzgame.tv.dashboard.demo.DemoDashboardTags
import com.quizzgame.tv.navigation.TvNavigationTags
import org.junit.Rule
import org.junit.Test

class DashboardNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val screen: AppShellScreen
        get() = AppShellScreen(compose)

    @Test
    fun tabsRetainDetailViewModelAndRecreationStateWhilePopDisposesTheEntry() {
        openDashboard()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        screen.tag(DemoDashboardTags.quiz(DashboardTabId.MAIN, 0)).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(DemoDashboardTags.DETAIL)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        screen.tag(DemoDashboardTags.COUNTER).assertTextEquals(compose.activity.getString(R.string.demo_counter, 1))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_UP)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(DemoDashboardTags.tabContent(DashboardTabId.LIBRARY))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_UP)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_LEFT)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(DemoDashboardTags.DETAIL)
        screen.tag(DemoDashboardTags.COUNTER).assertTextEquals(compose.activity.getString(R.string.demo_counter, 1))
        compose.activityRule.scenario.recreate()
        awaitVisible(DemoDashboardTags.DETAIL)
        screen.tag(DemoDashboardTags.COUNTER).assertTextEquals(compose.activity.getString(R.string.demo_counter, 1))
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        awaitVisible(DemoDashboardTags.tabContent(DashboardTabId.MAIN))
        screen.tag(DemoDashboardTags.quiz(DashboardTabId.MAIN, 0)).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(DemoDashboardTags.DETAIL)
        screen.tag(DemoDashboardTags.COUNTER).assertTextEquals(compose.activity.getString(R.string.demo_counter, 0))
    }

    @Test
    fun scrollingMainAndPoppingDetailRestoresTheFocusedCard() {
        openDashboard()
        repeat(7) { TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN) }
        screen.tag(DemoDashboardTags.quiz(DashboardTabId.MAIN, 6)).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(DemoDashboardTags.DETAIL)
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        awaitVisible(DemoDashboardTags.tabContent(DashboardTabId.MAIN))
        screen.tag(DemoDashboardTags.quiz(DashboardTabId.MAIN, 6)).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        screen.tag(DemoDashboardTags.tab(DashboardTabId.MAIN)).assertIsFocused()
    }

    @Test
    fun successfulLogoutDiscardsThePrivateDetailAndItsViewModel() {
        openDashboard()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        screen.tag(DemoDashboardTags.quiz(DashboardTabId.MAIN, 0)).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(DemoDashboardTags.DETAIL)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        screen.tag(DemoDashboardTags.SIGN_OUT).performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.waitUntil(5_000) { !compose.onNodeWithTag(DemoDashboardTags.DETAIL).isDisplayed() }
        screen.tag(DemoDashboardTags.DETAIL).assertDoesNotExist()
        awaitVisible(TvNavigationTags.DASHBOARD)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        screen.tag(DemoDashboardTags.quiz(DashboardTabId.MAIN, 0)).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(DemoDashboardTags.DETAIL)
        screen.tag(DemoDashboardTags.COUNTER).assertTextEquals(compose.activity.getString(R.string.demo_counter, 0))
    }

    private fun openDashboard() {
        awaitVisible(TvNavigationTags.DASHBOARD)
        screen.tag(DemoDashboardTags.tab(DashboardTabId.MAIN)).assertIsFocused()
    }

    private fun awaitVisible(tag: String) {
        compose.waitUntil(10_000) { compose.onNodeWithTag(tag).isDisplayed() }
    }
}
