package com.quizzgame.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.quizzgame.debug.network.mocks.ProfileMockScenario
import com.quizzgame.feature.account.tv.ui.tags.TvProfileTags
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags
import com.quizzgame.feature.authprovider.tv.ui.tags.TvDeviceAuthTags
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import com.quizzgame.feature.dashboard.tv.ui.tags.TvDashboardTags
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.feature.quizentry.tv.ui.tags.TvQuizEntryTags
import org.junit.Rule
import org.junit.Test
import java.io.File

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

    @Test
    fun remoteOpensGuestProfileAndBackRestoresTheProfileAction() {
        openSignInWithRemote()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        awaitVisible(TvProfileTags.SCREEN)
        compose.awaitFocused(TvProfileTags.SIGN_IN)
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(TvDashboardTags.PROFILE)
    }

    @Test
    fun remoteStartsGoogleDeviceSignInAndReturnsToSignedInProfile() {
        openSignInWithRemote()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.awaitFocused(TvSignInTags.SMS)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.awaitFocused(TvSignInTags.GOOGLE)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(TvDeviceAuthTags.CODE)
        awaitVisible(TvProfileTags.SIGN_OUT)
        signOutWithRemote()
    }

    @Test
    fun tvOtpOpensItsOwnScreenAndAcceptsHardwareDigits() {
        openSignInWithRemote()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        val auth = TvAuthTestScreen(compose)
        awaitVisible(TvSignInTags.IDENTIFIER)
        auth.identifier.performTextInput("guest@example.test")
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.awaitFocused(TvSignInTags.SEND)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)

        awaitVisible(TvSignInTags.CODE_SCREEN)
        awaitVisible(TvSignInTags.CODE)
        for (key in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_5) TvRemote.press(key)
        compose.awaitFocused(TvSignInTags.VERIFY)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(TvProfileTags.SIGN_OUT)
        signOutWithRemote()
    }

    @Test
    fun savesSuggestedNicknameFromTheRemoteAndRestoresFocus() {
        ProfileMockScenario.reset()
        openSignInWithRemote()
        repeat(2) { TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN) }
        compose.awaitFocused(TvSignInTags.GOOGLE)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(TvProfileTags.EDIT_NICKNAME)
        captureProfile(TvProfileTags.SCREEN, "tv-profile.png")
        compose.awaitFocused(TvProfileTags.SETTINGS)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_UP)
        compose.awaitFocused(TvProfileTags.EDIT_NICKNAME)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvProfileTags.SAVE_NICKNAME)
        captureProfile(TvProfileTags.NICKNAME_EDITOR, "tv-nickname.png")
        TvRemote.pressInDialog(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvProfileTags.EDIT_NICKNAME)
        val nodes = AppShellScreen(compose)
        nodes.tag(TvProfileTags.NICKNAME_EDITOR).assertDoesNotExist()
        nodes.tag(TvProfileTags.SIGN_OUT).performClick()
        awaitVisible(TvDashboardTags.NAVIGATION)
        ProfileMockScenario.reset()
    }

    private fun captureProfile(tag: String, filename: String) {
        val directory = checkNotNull(compose.activity.getExternalFilesDir("visual-previews"))
        File(directory, filename).outputStream().use { output ->
            val bitmap = compose.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    private fun openSignInWithRemote() {
        openDashboard()
        repeat(3) {
            val profile = compose.onNodeWithTag(TvDashboardTags.PROFILE).fetchSemanticsNode()
            if (!profile.config.getOrElse(SemanticsProperties.Focused) { false }) {
                TvRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
            }
        }
        compose.awaitFocused(TvDashboardTags.PROFILE)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvProfileTags.SIGN_IN)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.awaitFocused(TvSignInTags.EMAIL)
    }

    private fun signOutWithRemote() {
        compose.awaitFocused(TvProfileTags.SETTINGS)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        compose.awaitFocused(TvProfileTags.SIGN_OUT)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible(TvDashboardTags.NAVIGATION)
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
