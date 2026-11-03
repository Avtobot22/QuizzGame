package com.quizzgame.mobile

import android.os.Bundle
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import com.quizzgame.core.navigation.ProcessNavigationGuard
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class RootNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun signInCompletionReplacesSplashAndSurvivesActivityRecreationThenRootBackExits() {
        awaitVisible("splash")
        compose.onNodeWithTag("demo-action-0").performClick()
        awaitVisible("signin")
        compose.onNodeWithTag("demo-action-0").performClick()
        awaitVisible("join-room")
        compose.activityRule.scenario.recreate()
        awaitVisible("join-room")
        Espresso.pressBackUnconditionally()
        compose.waitUntil(5_000) { compose.activityRule.scenario.state == Lifecycle.State.DESTROYED }
    }

    @Test
    fun savedStateFromAnotherProcessIsDroppedBeforeNavigationRestores() {
        val stale = Bundle().apply { putString("quizz.navigation.process", "previous-process") }
        assertNull(ProcessNavigationGuard.restore(stale))
        val current = Bundle()
        ProcessNavigationGuard.save(current)
        assertSame(current, ProcessNavigationGuard.restore(current))
    }

    private fun awaitVisible(tag: String) {
        compose.waitUntil(10_000) { compose.onNodeWithTag(tag).isDisplayed() }
    }
}
