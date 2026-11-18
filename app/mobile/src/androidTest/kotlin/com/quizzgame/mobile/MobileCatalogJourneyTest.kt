package com.quizzgame.mobile

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.Espresso.pressBack
import com.quizzgame.debug.network.mocks.SplashMockScenario
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

class MobileCatalogJourneyTest {
    private val compose = createAndroidComposeRule<MainActivity>()
    private val scenario = object : ExternalResource() {
        override fun before() {
            SplashMockScenario.reset()
            MobileCatalogMockScenario.reset()
        }
        override fun after() {
            SplashMockScenario.reset()
            MobileCatalogMockScenario.reset()
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
    fun connectionAttemptDoesNotPretendToJoinRoom() {
        await(MobilePlayTags.CONNECT)
        screen.tag(MobilePlayTags.CONNECT).performClick()
        await(MobilePlayTags.UNAVAILABLE)
        screen.tag(MobilePlayTags.UNAVAILABLE).assertIsDisplayed()
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
