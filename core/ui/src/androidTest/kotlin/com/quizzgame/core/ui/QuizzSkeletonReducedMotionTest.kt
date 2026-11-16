package com.quizzgame.core.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QuizzSkeletonReducedMotionTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>(effectContext = TestMotionDurationScale(0f))

    @Test
    fun reducedMotionKeepsSkeletonStaticAndAccessible() {
        compose.mainClock.autoAdvance = false
        compose.setSkeleton()
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        val screen = SkeletonScreen(compose)
        screen.assertAccessibleLoadingWithoutFocusTargets()
        val first = screen.frame()
        compose.mainClock.advanceTimeBy(5000)
        compose.waitForIdle()
        assertTrue(first.sameAs(screen.frame()))
    }
}
