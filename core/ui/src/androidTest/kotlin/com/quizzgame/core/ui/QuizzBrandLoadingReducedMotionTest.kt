package com.quizzgame.core.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QuizzBrandLoadingReducedMotionTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>(effectContext = TestMotionDurationScale(0f))

    @Test
    fun disabledAnimationsRenderACompleteStaticLoaderWithOneAccessibleProgressState() {
        compose.mainClock.autoAdvance = false
        compose.setBrandLoading()
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        val screen = BrandLoadingScreen(compose)
        screen.assertSingleStableAnnouncement()
        val first = screen.captureVisibleFrame()
        compose.mainClock.advanceTimeBy(12_000)
        compose.waitForIdle()
        screen.assertSingleStableAnnouncement()
        val later = screen.captureVisibleFrame()
        assertTrue("Reduced motion must not change the rendered frame", first.sameAs(later))
    }
}
