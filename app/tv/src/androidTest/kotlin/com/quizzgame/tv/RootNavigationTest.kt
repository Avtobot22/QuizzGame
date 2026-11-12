package com.quizzgame.tv

import android.os.Bundle
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import com.quizzgame.tv.navigation.TvNavigationTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RootNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun successfulSplashResetsToContentAndSurvivesActivityRecreationThenRootBackExits() {
        awaitVisible(TvNavigationTags.DASHBOARD)
        compose.activityRule.scenario.recreate()
        awaitVisible(TvNavigationTags.DASHBOARD)
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
