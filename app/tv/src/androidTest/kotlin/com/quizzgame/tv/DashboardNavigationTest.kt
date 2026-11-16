package com.quizzgame.tv

import android.view.KeyEvent
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import com.quizzgame.feature.dashboard.tv.ui.tags.TvDashboardTags
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.feature.quizentry.tv.ui.tags.TvQuizEntryTags
import org.junit.Rule
import org.junit.Test

class DashboardNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()
    private val screen get() = CatalogScreen(compose)

    @Test
    fun selectingFeedOpensEntryAndBackRestoresCardThenTabFocus() {
        openDashboard()
        screen.settingsTab.assertIsNotEnabled()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvFeedTags.card(CatalogScreen.FIRST_CARD_ID))
        screen.firstCard.assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(TvQuizEntryTags.CONTENT)
        screen.bar.assertDoesNotExist()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(TvFeedTags.card(CatalogScreen.FIRST_CARD_ID))
        screen.firstCard.assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(TvDashboardTags.tab(DashboardTabId.FEED))
        screen.feedTab.assertIsFocused()
    }

    @Test
    fun entrySurvivesRecreationAndBackReturnsToCachedFeed() {
        openDashboard()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvFeedTags.card(CatalogScreen.FIRST_CARD_ID))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(TvQuizEntryTags.CONTENT)
        compose.activityRule.scenario.recreate()
        awaitVisible(TvQuizEntryTags.CONTENT)
        screen.bar.assertDoesNotExist()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(TvFeedTags.card(CatalogScreen.FIRST_CARD_ID))
        screen.firstCard.assertIsFocused()
    }

    @Test
    fun appendedCardRestoresAfterQuizEntryAndSwitchingTabs() {
        openDashboard()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvFeedTags.card(CatalogScreen.FIRST_CARD_ID))
        val appended = "18e2b5b5-05e6-43f5-9580-2e88ec713004"
        repeat(2) { TvRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT) }
        compose.awaitFocused(TvFeedTags.card(appended))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(TvQuizEntryTags.CONTENT)
        screen.bar.assertDoesNotExist()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(TvFeedTags.card(appended))
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(TvDashboardTags.tab(DashboardTabId.FEED))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvDashboardTags.tab(DashboardTabId.LIBRARY))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_LEFT)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvFeedTags.card(appended))
    }

    @Test
    fun verticalAppendAndActivityRecreationRestoreTheLastSectionCard() {
        openDashboard()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvFeedTags.card(CatalogScreen.FIRST_CARD_ID))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.awaitFocused(TvFeedTags.card("18e2b5b5-05e6-43f5-9580-2e88ec713003"))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        val last = "18e2b5b5-05e6-43f5-9580-2e88ec713005"
        compose.awaitFocused(TvFeedTags.card(last))
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(TvQuizEntryTags.CONTENT)
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(TvFeedTags.card(last))
        compose.activityRule.scenario.recreate()
        compose.awaitFocused(TvFeedTags.card(last))
    }

    private fun openDashboard() {
        awaitVisible(TvDashboardTags.NAVIGATION)
        compose.awaitFocused(TvDashboardTags.tab(DashboardTabId.FEED))
        screen.feedTab.assertIsFocused()
    }

    private fun awaitVisible(tag: String) {
        compose.waitUntil(10_000) { compose.onNodeWithTag(tag).isDisplayed() }
    }
}
