package com.quizzgame.core.error.ui.tv

import android.view.KeyEvent
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
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.error.ui.common.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzTvErrorSizingTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun largeFontInShortWindowScrollsFocusedRecoveryActionIntoView() {
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                QuizzTvTheme {
                    Box(Modifier.size(width = 320.dp, height = 240.dp)) {
                        QuizzTvErrorFullScreen(
                            ErrorPresentationSpec(
                                surface = ErrorSurface.BLOCKING,
                                title = ErrorCopyKey.SESSION_EXPIRED,
                                message = ErrorCopyKey.CONNECTION_PROBLEM,
                                actions = listOf(ErrorActionKind.SIGN_IN),
                            ),
                            onAction = actions::add,
                        )
                    }
                }
            }
        }

        TvErrorScreen(compose).action(compose.activity.getString(R.string.quizz_error_action_sign_in)).apply {
            assertIsFocused()
            assertIsDisplayed()
        }
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals(listOf(ErrorActionKind.SIGN_IN), actions) }
        captureErrorPreview(compose.onRoot(), "tv-large-font.png")
    }
}
