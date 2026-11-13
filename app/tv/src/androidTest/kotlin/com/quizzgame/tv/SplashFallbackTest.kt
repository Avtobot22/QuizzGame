package com.quizzgame.tv

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.quizzgame.debug.network.mocks.SplashMockScenario
import com.quizzgame.tv.navigation.TvNavigationTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

class SplashFallbackTest {
    private val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var attempt: SplashMockScenario.Attempt
    private val fixture = object : ExternalResource() {
        override fun before() { attempt = SplashMockScenario.emptyRemoteFlags() }
        override fun after() { SplashMockScenario.reset() }
    }

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(fixture).around(compose)

    @Test
    fun validEmptyRemoteUsesPackagedAssetAndRoutesGuestToSignIn() {
        compose.waitUntil(10_000) { compose.onNodeWithTag(TvNavigationTags.SIGN_IN).isDisplayed() }
        AppShellScreen(compose).tag(TvNavigationTags.SIGN_IN).assertIsDisplayed()
        assertEquals(1, attempt.count(SplashMockScenario.Stage.LAUNCH))
        assertEquals(1, attempt.count(SplashMockScenario.Stage.FLAGS))
        assertEquals(0, attempt.count(SplashMockScenario.Stage.USER))
    }
}
