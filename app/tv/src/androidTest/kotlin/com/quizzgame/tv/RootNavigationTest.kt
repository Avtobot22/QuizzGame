package com.quizzgame.tv

import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RootNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun signInCompletionReplacesSplashAndSurvivesActivityRecreationThenRootBackExits() {
        awaitVisible("splash")
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("signin")
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        awaitVisible("dashboard")
        compose.activityRule.scenario.recreate()
        awaitVisible("dashboard")
        Espresso.pressBackUnconditionally()
        compose.waitUntil(5_000) { compose.activityRule.scenario.state == Lifecycle.State.DESTROYED }
    }

    @Test
    fun activityRecreationRetainsSavedStateOutsideTheNavigationOwner() {
        compose.activityRule.scenario.onActivity { activity ->
            activity.savedStateRegistry.registerSavedStateProvider("unrelated-draft") {
                Bundle().apply { putString("draft", "retained") }
            }
        }
        compose.activityRule.scenario.recreate()
        compose.activityRule.scenario.onActivity { activity ->
            assertEquals(
                "retained",
                activity.savedStateRegistry.consumeRestoredStateForKey("unrelated-draft")?.getString("draft"),
            )
        }
    }

    private fun awaitVisible(tag: String) {
        compose.waitUntil(10_000) { compose.onNodeWithTag(tag).isDisplayed() }
    }
}
