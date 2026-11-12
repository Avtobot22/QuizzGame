package com.quizzgame.tv

import android.view.KeyEvent
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import com.quizzgame.debug.network.mocks.SplashMockScenario
import com.quizzgame.tv.navigation.TvNavigationTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import com.quizzgame.core.error.ui.common.R as ErrorR

class SplashRetryTest {
    private val compose = createAndroidComposeRule<MainActivity>()
    private val failure = SplashFailureRule()

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(failure).around(compose)

    private val screen: AppShellScreen
        get() = AppShellScreen(compose)

    @Test
    fun failedFlagsLoadSurvivesRecreationAndRapidRetryRunsOneNewStartup() {
        awaitRetry()
        compose.activityRule.scenario.recreate()
        awaitRetry()
        assertEquals(1, failure.attempt.count(SplashMockScenario.Stage.LAUNCH))
        assertEquals(1, failure.attempt.count(SplashMockScenario.Stage.FLAGS))
        val retry = compose.activity.getString(ErrorR.string.quizz_error_action_retry)
        screen.text(retry).assertIsFocused()
        TvRemote.pressBeforeNextFrame(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_DPAD_CENTER)
        compose.waitUntil(10_000) { compose.onNodeWithTag(TvNavigationTags.DASHBOARD).isDisplayed() }
        screen.tag(TvNavigationTags.DASHBOARD).assertIsDisplayed()
        assertEquals(2, failure.attempt.count(SplashMockScenario.Stage.LAUNCH))
        assertEquals(2, failure.attempt.count(SplashMockScenario.Stage.FLAGS))
        Espresso.pressBackUnconditionally()
        awaitDestroyed()
    }

    @Test
    fun exitFromSplashErrorClosesTheRootActivity() {
        awaitRetry()
        val retry = compose.activity.getString(ErrorR.string.quizz_error_action_retry)
        val exit = compose.activity.getString(ErrorR.string.quizz_error_action_exit)
        screen.text(retry).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        screen.text(exit).assertIsFocused()
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        screen.text(exit).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitDestroyed()
        assertEquals(1, failure.attempt.count(SplashMockScenario.Stage.FLAGS))
    }

    @Test
    fun backFromSplashErrorClosesTheRootActivity() {
        awaitRetry()
        Espresso.pressBackUnconditionally()
        awaitDestroyed()
        assertEquals(1, failure.attempt.count(SplashMockScenario.Stage.FLAGS))
    }

    private fun awaitRetry() {
        val retry = compose.activity.getString(ErrorR.string.quizz_error_action_retry)
        compose.waitUntil(10_000) { compose.onNodeWithText(retry).isDisplayed() }
        screen.text(retry).assertIsDisplayed()
    }

    private fun awaitDestroyed() {
        compose.waitUntil(5_000) { compose.activityRule.scenario.state == Lifecycle.State.DESTROYED }
    }
}

private class SplashFailureRule : ExternalResource() {
    lateinit var attempt: SplashMockScenario.Attempt
        private set

    override fun before() { attempt = SplashMockScenario.failOnce(SplashMockScenario.Stage.FLAGS) }

    override fun after() { SplashMockScenario.reset() }
}
