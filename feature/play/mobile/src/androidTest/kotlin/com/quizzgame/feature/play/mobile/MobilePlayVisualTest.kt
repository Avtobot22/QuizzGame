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
import com.quizzgame.feature.play.mobile.presentation.MobilePlayJoinIdentity
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState
import com.quizzgame.feature.play.mobile.presentation.MobilePlayValidation
import com.quizzgame.feature.play.mobile.ui.MobilePlayContent
import com.quizzgame.feature.play.mobile.ui.tags.MobilePlayTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MobilePlayVisualTest(private val dark: Boolean, private val fontScale: Float) {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun joinFormRetainsPinAndShowsCameraFallbackAtLargeText() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(3f, fontScale)) {
                QuizzMobileTheme(darkTheme = dark) {
                    var state by remember { mutableStateOf(MobilePlayState(code = "031482", nickname = "Саша")) }
                    Box(Modifier.fillMaxSize().testTag(MobilePlayVisualScreen.ROOT)) {
                        MobilePlayContent(
                            state,
                            {},
                            onScan = { state = state.copy(validation = MobilePlayValidation.CAMERA_DENIED) },
                        )
                    }
                }
            }
        }
        val screen = MobilePlayVisualScreen(compose)
        screen.capture(MobilePlayVisualScreen.ROOT, "mobile-play-$dark-$fontScale.png")
        screen.tag(MobilePlayTags.SCAN).performScrollTo()
        screen.tag(MobilePlayTags.SCAN).performClick()
        screen.tag(MobilePlayTags.VALIDATION).performScrollTo()
        screen.tag(MobilePlayTags.VALIDATION).assertIsDisplayed()
        screen.tag(MobilePlayTags.CODE).performScrollTo()
        screen.tag(MobilePlayTags.CODE).assertTextContains("031482")
    }

    @Test
    fun signedInPlayerCanContinueWithoutANicknameField() {
        val sent = mutableListOf<MobilePlayIntent>()
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(3f, fontScale)) {
                QuizzMobileTheme(darkTheme = dark) {
                    MobilePlayContent(
                        MobilePlayState(code = "031482", identity = MobilePlayJoinIdentity.Account(1, "Майя")),
                        sent::add,
                    )
                }
            }
        }
        val screen = MobilePlayVisualScreen(compose)
        screen.tag(MobilePlayTags.NICKNAME).assertDoesNotExist()
        screen.tag(MobilePlayTags.CONNECT).performScrollTo()
        screen.tag(MobilePlayTags.CONNECT).performClick()
        compose.runOnIdle { assertEquals(listOf(MobilePlayIntent.Connect), sent) }
        screen.capture(MobilePlayTags.ROOT, "mobile-play-account-$dark-$fontScale.png")
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "dark {0}, font scale {1}")
        fun states(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            listOf(arrayOf<Any>(dark, 1f), arrayOf<Any>(dark, 1.6f))
        }
    }
}
