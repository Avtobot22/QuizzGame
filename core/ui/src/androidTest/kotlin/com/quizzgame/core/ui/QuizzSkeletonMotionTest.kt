package com.quizzgame.core.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class QuizzSkeletonMotionTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>(effectContext = TestMotionDurationScale(1f))

    @Test
    fun shimmerMovesWhileAccessibleLoadingRemainsStable() {
        compose.mainClock.autoAdvance = false
        compose.setSkeleton()
        compose.mainClock.advanceTimeBy(300)
        compose.waitForIdle()
        val screen = SkeletonScreen(compose)
        screen.assertAccessibleLoadingWithoutFocusTargets()
        val first = screen.frame()
        compose.mainClock.advanceTimeBy(700)
        compose.waitForIdle()
        screen.assertAccessibleLoadingWithoutFocusTargets()
        assertFalse(first.sameAs(screen.frame()))
    }
}
