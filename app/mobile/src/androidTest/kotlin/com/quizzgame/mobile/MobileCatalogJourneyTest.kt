package com.quizzgame.mobile

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.Espresso.pressBack
import com.quizzgame.debug.network.mocks.ProfileMockScenario
import com.quizzgame.debug.network.mocks.SplashMockScenario
import com.quizzgame.feature.account.mobile.navigation.ProfileRoute
import com.quizzgame.feature.account.mobile.ui.tags.MobileProfileTags
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags
import com.quizzgame.feature.dashboard.mobile.navigation.LibraryRoute
import com.quizzgame.feature.feed.mobile.navigation.ExploreRoute
import com.quizzgame.feature.feed.mobile.ui.tags.MobileFeedTags
import com.quizzgame.feature.play.mobile.navigation.PlayRoute
import com.quizzgame.feature.play.mobile.ui.tags.MobilePlayTags
import com.quizzgame.feature.quizentry.mobile.ui.tags.MobileQuizEntryTags
import com.quizzgame.mobile.network.mocks.MobileCatalogMockScenario
import com.quizzgame.mobile.ui.tags.MobileChromeTags
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import java.io.File
import com.quizzgame.feature.dashboard.mobile.R as DashboardR

class MobileCatalogJourneyTest {
    private val compose = createAndroidComposeRule<MainActivity>()
    private val scenario = object : ExternalResource() {
        override fun before() {
            SplashMockScenario.reset()
            MobileCatalogMockScenario.reset()
            ProfileMockScenario.reset()
        }
        override fun after() {
            SplashMockScenario.reset()
            MobileCatalogMockScenario.reset()
            ProfileMockScenario.reset()
        }
    }

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(scenario).around(compose)
    private val screen get() = MobileCatalogScreen(compose)

    @Test
    fun exploreOpensDetailsByEntryIdAndBackRestoresFeedThenCtaOpensPlay() {
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(ExploreRoute)).performClick()
        val hero = MobileFeedTags.card("ebc9675e-2cd9-4ed7-a4b4-1d279d2ed002")
        await(hero)
        screen.tag(hero).performClick()
        awaitEntryAction()
        screen.tag(MobileChromeTags.BAR).assertDoesNotExist()
        pressBack()
        await(hero)
        screen.tag(hero).performClick()
        awaitEntryAction()
        screen.tag(MobileQuizEntryTags.CONNECT).performClick()
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(PlayRoute)).assertIsDisplayed()
    }

    @Test
    fun emptyPinShowsValidationAndKeepsJoinForm() {
        compose.waitUntil(15_000) {
            compose.onAllNodes(hasTestTag(MobilePlayTags.CONNECT) and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        screen.tag(MobilePlayTags.CONNECT).performClick()
        screen.tag(MobilePlayTags.VALIDATION, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun dashboardFailureKeepsLocalNavigationUsable() {
        // The next Activity recreation requests chrome again in its own store.
        MobileCatalogMockScenario.failDashboard(MobileCatalogMockScenario.Failure.SERVER)
        compose.activityRule.scenario.recreate()
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(ExploreRoute)).performClick()
        await(MobileFeedTags.LIST)
    }

    @Test
    fun pullToRefreshReloadsTheFirstPageAndKeepsContentUsable() {
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(ExploreRoute)).performClick()
        await(MobileFeedTags.LIST)
        val before = MobileCatalogMockScenario.feedLoadCount
        screen.tag(MobileFeedTags.REFRESH).performTouchInput { swipeDown() }
        compose.waitUntil(15_000) { MobileCatalogMockScenario.feedLoadCount > before }
        await(MobileFeedTags.LIST)
    }

    @Test
    fun libraryShowsItsOwnStatusAndProfileOpensSeparately() {
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(LibraryRoute)).performClick()
        screen.text(compose.activity.getString(DashboardR.string.library_unavailable)).assertIsDisplayed()
        screen.tag(MobileProfileTags.SCREEN).assertDoesNotExist()

        screen.tag(MobileChromeTags.item(ProfileRoute)).performClick()
        await(MobileProfileTags.SIGN_IN)
    }

    @Test
    fun guestCanCancelSignInThenUseOtpAndReturnToProfile() {
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(ProfileRoute)).performClick()
        await(MobileProfileTags.SIGN_IN)
        captureScreen(MobileProfileTags.SCREEN, "mobile-profile-guest.png")
        screen.tag(MobileProfileTags.SIGN_IN).performClick()
        await(MobileSignInTags.EMAIL)
        captureScreen(MobileSignInTags.SCREEN, "mobile-auth-methods.png")
        pressBack()
        await(MobileProfileTags.SIGN_IN)
        screen.tag(MobileProfileTags.SIGN_IN).performClick()
        await(MobileSignInTags.EMAIL)
        screen.tag(MobileSignInTags.EMAIL).performClick()
        captureScreen(MobileSignInTags.SCREEN, "mobile-auth-email.png")
        screen.tag(MobileSignInTags.IDENTIFIER).performTextInput("guest@example.test")
        screen.tag(MobileSignInTags.SEND).performClick()
        await(MobileSignInTags.CODE)
        captureScreen(MobileSignInTags.SCREEN, "mobile-auth-code.png")
        screen.tag(MobileSignInTags.CODE).performTextInput("012345")
        screen.tag(MobileSignInTags.VERIFY).performClick()
        await(MobileProfileTags.SIGN_OUT)
        screen.tag(MobileProfileTags.SIGN_OUT).performClick()
        await(MobilePlayTags.ROOT)
    }

    @Test
    fun wrongCodeKeepsInputAndAllowsChangingEmailDuringCooldown() {
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(ProfileRoute)).performClick()
        await(MobileProfileTags.SIGN_IN)
        screen.tag(MobileProfileTags.SIGN_IN).performClick()
        await(MobileSignInTags.EMAIL)
        screen.tag(MobileSignInTags.EMAIL).performClick()
        screen.tag(MobileSignInTags.IDENTIFIER).performTextInput("guest@example.test")
        screen.tag(MobileSignInTags.SEND).performClick()

        await(MobileSignInTags.CODE)
        screen.tag(MobileSignInTags.RESEND_TIMER).assertIsDisplayed()
        screen.tag(MobileSignInTags.RESEND).assertIsNotEnabled()
        screen.tag(MobileSignInTags.CODE).performTextInput("111111")
        screen.tag(MobileSignInTags.VERIFY).performClick()
        await(MobileSignInTags.ERROR)
        captureScreen(MobileSignInTags.SCREEN, "mobile-auth-code-error.png")
        screen.tag(MobileSignInTags.CODE).assertIsDisplayed()
        screen.tag(MobileSignInTags.VERIFY).assertIsNotEnabled()
        screen.tag(MobileSignInTags.CHANGE_DESTINATION).apply {
            performScrollTo()
            performClick()
        }
        await(MobileSignInTags.IDENTIFIER)
    }

    @Test
    fun switchingFromEmailToSmsKeepsTheServerCooldown() {
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(ProfileRoute)).performClick()
        await(MobileProfileTags.SIGN_IN)
        screen.tag(MobileProfileTags.SIGN_IN).performClick()
        await(MobileSignInTags.EMAIL)
        screen.tag(MobileSignInTags.EMAIL).performClick()
        screen.tag(MobileSignInTags.IDENTIFIER).performTextInput("guest@example.test")
        screen.tag(MobileSignInTags.SEND).performClick()
        await(MobileSignInTags.CODE)
        screen.tag(MobileSignInTags.CODE).performTextInput("999999")
        screen.tag(MobileSignInTags.VERIFY).performClick()
        await(MobileSignInTags.ERROR)

        screen.tag(MobileSignInTags.CHANGE_DESTINATION).apply {
            performScrollTo()
            performClick()
        }
        await(MobileSignInTags.IDENTIFIER)
        pressBack()
        await(MobileSignInTags.SMS)
        screen.tag(MobileSignInTags.SMS).performClick()
        screen.tag(MobileSignInTags.IDENTIFIER).performTextInput("+79123456789")
        screen.tag(MobileSignInTags.SEND).assertIsNotEnabled()
    }

    @Test
    fun savesSuggestedNicknameAndReadsItWhenReturningToPlay() {
        await(MobilePlayTags.ROOT)
        screen.tag(MobileChromeTags.item(ProfileRoute)).performClick()
        await(MobileProfileTags.SIGN_IN)
        screen.tag(MobileProfileTags.SIGN_IN).performClick()
        await(MobileSignInTags.EMAIL)
        screen.tag(MobileSignInTags.EMAIL).performClick()
        screen.tag(MobileSignInTags.IDENTIFIER).performTextInput("guest@example.test")
        screen.tag(MobileSignInTags.SEND).performClick()
        await(MobileSignInTags.CODE)
        screen.tag(MobileSignInTags.CODE).performTextInput("012345")
        screen.tag(MobileSignInTags.VERIFY).performClick()

        await(MobileProfileTags.EDIT_NICKNAME)
        captureScreen(MobileProfileTags.SCREEN, "mobile-profile.png")
        screen.tag(MobileProfileTags.EDIT_NICKNAME).performClick()
        await(MobileProfileTags.NICKNAME_FIELD)
        val generated = compose.onNodeWithTag(MobileProfileTags.NICKNAME_FIELD).fetchSemanticsNode()
            .config[SemanticsProperties.EditableText].text
        check(generated.startsWith("Quiz"))
        captureScreen(MobileProfileTags.NICKNAME_EDITOR, "mobile-nickname.png")
        screen.tag(MobileProfileTags.SAVE_NICKNAME).performClick()
        screen.tag(MobileProfileTags.NICKNAME_EDITOR).assertDoesNotExist()

        screen.tag(MobileChromeTags.item(PlayRoute)).performClick()
        await(MobilePlayTags.PROFILE_NAME)
        screen.tag(MobilePlayTags.PROFILE_NAME).assertTextContains(generated, substring = true)
        screen.tag(MobilePlayTags.NICKNAME).assertDoesNotExist()
        screen.tag(MobileChromeTags.item(ProfileRoute)).performClick()
        await(MobileProfileTags.SIGN_OUT)
        screen.tag(MobileProfileTags.SIGN_OUT).performScrollTo()
        screen.tag(MobileProfileTags.SIGN_OUT).performClick()
        await(MobilePlayTags.ROOT)
    }

    private fun captureScreen(tag: String, filename: String) {
        val directory = checkNotNull(compose.activity.getExternalFilesDir("visual-previews"))
        File(directory, filename).outputStream().use { output ->
            val bitmap = compose.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    private fun awaitEntryAction() {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithTag(MobileQuizEntryTags.CONNECT).fetchSemanticsNodes().isNotEmpty()
        }
        screen.tag(MobileQuizEntryTags.CONNECT).performScrollTo()
    }

    private fun await(tag: String) {
        compose.waitUntil(15_000) { compose.onNodeWithTag(tag).isDisplayed() }
    }
}
