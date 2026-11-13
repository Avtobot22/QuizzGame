package com.quizzgame.core.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class QuizzBrandLoadingMotionTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>(effectContext = TestMotionDurationScale(1f))

    @Test
    fun enabledMotionChangesVisualFramesWithoutChangingAccessibleProgressOrAddingFocusTargets() {
        compose.mainClock.autoAdvance = false
        compose.setBrandLoading(QuizzBrandLoadingSize.EXPANDED)
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        val screen = BrandLoadingScreen(compose)
        screen.assertSingleStableAnnouncement()
        val first = screen.captureVisibleFrame()
        compose.mainClock.advanceTimeBy(1_700)
        compose.waitForIdle()
        screen.assertSingleStableAnnouncement()
        assertFalse("Enabled motion must remain visible", first.sameAs(screen.captureVisibleFrame()))
    }
}
