package com.quizzgame.feature.play.mobile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.feature.play.mobile.presentation.MobilePlayIntent
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState
import com.quizzgame.feature.play.mobile.ui.MobilePlayContent
import com.quizzgame.feature.play.mobile.ui.tags.MobilePlayTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MobilePlayVisualTest(private val dark: Boolean, private val fontScale: Float) {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun connectionFormAndInsetDialogRemainUsableWithLargeText() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(3f, fontScale)) {
                QuizzMobileTheme(darkTheme = dark) {
                    var state by remember { mutableStateOf(MobilePlayState.READY) }
                    Box(Modifier.fillMaxSize().testTag(MobilePlayVisualScreen.ROOT)) {
                        MobilePlayContent(state, { intent ->
                            state = if (intent == MobilePlayIntent.DISMISS) {
                                MobilePlayState.READY
                            } else {
                                MobilePlayState.UNAVAILABLE
                            }
                        })
                    }
                }
            }
        }
        val screen = MobilePlayVisualScreen(compose)
        screen.capture(MobilePlayVisualScreen.ROOT, "mobile-play-$dark-$fontScale.png")
        screen.tag(MobilePlayTags.SCAN).performScrollTo()
        screen.tag(MobilePlayTags.SCAN).performClick()
        screen.tag(MobilePlayTags.UNAVAILABLE).assertIsDisplayed()
        val pageWidth = screen.bounds(MobilePlayVisualScreen.ROOT).width
        val dialogWidth = screen.bounds(MobilePlayTags.UNAVAILABLE).width
        screen.capture(MobilePlayTags.UNAVAILABLE, "mobile-dialog-$dark-$fontScale.png")
        val deviceDensity = compose.activity.resources.displayMetrics.density
        assertTrue(
            "Dialog width $dialogWidth must leave 28dp on either side of $pageWidth",
            dialogWidth <= pageWidth - 56f * deviceDensity + 1f,
        )
    }
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "dark {0}, font scale {1}")
        fun states(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            listOf(arrayOf<Any>(dark, 1f), arrayOf<Any>(dark, 1.6f))
        }
    }
}
