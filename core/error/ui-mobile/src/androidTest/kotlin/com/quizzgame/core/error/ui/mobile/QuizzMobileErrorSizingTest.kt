package com.quizzgame.core.error.ui.mobile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.error.ui.common.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzMobileErrorSizingTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun largeFontInShortWindowKeepsLastRecoveryActionReachable() {
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                QuizzMobileTheme {
                    Box(Modifier.size(width = 320.dp, height = 240.dp)) {
                        QuizzMobileErrorFullScreen(
                            ErrorPresentationSpec(
                                surface = ErrorSurface.BLOCKING,
                                title = ErrorCopyKey.SESSION_EXPIRED,
                                message = ErrorCopyKey.CONNECTION_PROBLEM,
                                actions = listOf(ErrorActionKind.SIGN_IN, ErrorActionKind.OPEN_SETTINGS),
                            ),
                            onAction = actions::add,
                        )
                    }
                }
            }
        }

        MobileErrorScreen(compose).action(compose.activity.getString(R.string.quizz_error_action_open_settings)).apply {
            performScrollTo()
            assertIsDisplayed()
            performClick()
        }
        compose.runOnIdle { assertEquals(listOf(ErrorActionKind.OPEN_SETTINGS), actions) }
        captureErrorPreview(compose.onRoot(), "mobile-large-font.png")
    }
}
