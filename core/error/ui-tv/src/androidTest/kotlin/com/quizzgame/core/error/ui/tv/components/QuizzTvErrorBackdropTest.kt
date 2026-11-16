package com.quizzgame.core.error.ui.tv.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QuizzTvErrorBackdropTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun darkFullScreenStaysDarkInsideALightOwner() {
        compose.setContent {
            QuizzTvTheme {
                Box(Modifier.fillMaxSize().background(Color.White)) {
                    QuizzTvErrorFullScreen(
                        ErrorPresentationSpec(ErrorSurface.BLOCKING, ErrorCopyKey.CONNECTION_PROBLEM),
                        onAction = {},
                    )
                }
            }
        }
        val pixels = compose.onRoot().captureToImage().toPixelMap()
        val background = pixels[(pixels.width * 0.2f).toInt(), pixels.height / 2]
        assertTrue(
            "The full-screen backdrop must not expose the owner's white background",
            background.luminance() < 0.1f,
        )
    }
}
