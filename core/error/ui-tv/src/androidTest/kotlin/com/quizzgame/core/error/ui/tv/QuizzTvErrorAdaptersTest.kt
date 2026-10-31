package com.quizzgame.core.error.ui.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.error.ui.common.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzTvErrorAdaptersTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: TvErrorScreen
        get() = TvErrorScreen(compose)

    @Test
    fun blockingFocusesFirstActionAndCenterReportsConfirmationIntent() {
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            QuizzTvTheme {
                QuizzTvErrorFullScreen(
                    spec(ErrorSurface.BLOCKING),
                    onAction = actions::add,
                    modifier = Modifier.testTag("error"),
                )
            }
        }

        screen.action(copy(R.string.quizz_error_action_confirm_retry)).assertIsFocused()
        screen.assertPanelHorizontallyCentered()
        captureErrorPreview(compose.onRoot(), "tv-blocking.png")
        compose.runOnIdle { assertEquals(emptyList<ErrorActionKind>(), actions) }
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals(listOf(ErrorActionKind.CONFIRM_RETRY), actions) }
    }

    @Test
    fun remoteMovesBetweenSuppliedActionsBeforeActivatingFocusedAction() {
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            QuizzTvTheme {
                QuizzTvErrorFullScreen(spec(ErrorSurface.BLOCKING), onAction = actions::add)
            }
        }

        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        screen.action(copy(R.string.quizz_error_action_dismiss)).assertIsFocused()
        screen.action(copy(R.string.quizz_error_action_confirm_retry)).assertIsNotFocused()
        compose.runOnIdle { assertEquals(emptyList<ErrorActionKind>(), actions) }
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals(listOf(ErrorActionKind.DISMISS), actions) }
        screen.text(copy(R.string.quizz_error_action_retry)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_action_go_back)).assertDoesNotExist()
    }

    @Test
    fun updatingBlockingCopyPreservesCurrentActionFocus() {
        val current = mutableStateOf(spec(ErrorSurface.BLOCKING))
        compose.setContent {
            QuizzTvTheme { QuizzTvErrorFullScreen(current.value, onAction = {}) }
        }

        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_RIGHT)
        screen.action(copy(R.string.quizz_error_action_dismiss)).assertIsFocused()
        compose.runOnIdle { current.value = current.value.copy(title = ErrorCopyKey.REQUEST_TIMED_OUT) }
        screen.text(copy(R.string.quizz_error_request_timed_out)).assertIsDisplayed()
        screen.action(copy(R.string.quizz_error_action_dismiss)).assertIsFocused()
    }

    @Test
    fun disabledActionsIgnoreInputAndReceiveInitialFocusWhenEnabled() {
        val enabled = mutableStateOf(false)
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            QuizzTvTheme {
                QuizzTvErrorFullScreen(
                    spec(ErrorSurface.BLOCKING),
                    onAction = actions::add,
                    enabled = enabled.value,
                )
            }
        }

        screen.action(copy(R.string.quizz_error_action_confirm_retry)).assertIsNotEnabled()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        screen.action(copy(R.string.quizz_error_action_confirm_retry)).assertIsFocused()
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle {
            assertEquals(emptyList<ErrorActionKind>(), actions)
            enabled.value = true
        }
        screen.action(copy(R.string.quizz_error_action_confirm_retry)).apply {
            assertIsEnabled()
            assertIsFocused()
        }
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle { assertEquals(listOf(ErrorActionKind.CONFIRM_RETRY), actions) }
    }

    @Test
    fun bannerPreservesHostContentAndRemoteFocus() {
        val showBanner = mutableStateOf(false)
        var hostClicks = 0
        val errorActions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            QuizzTvTheme {
                Column {
                    FocusedHost(onClick = { hostClicks++ })
                    if (showBanner.value) {
                        QuizzTvErrorBanner(spec(ErrorSurface.BANNER), onAction = errorActions::add)
                    }
                }
            }
        }

        screen.host.assertIsFocused()
        compose.runOnIdle { showBanner.value = true }
        screen.text(copy(R.string.quizz_error_server_unavailable)).assertIsDisplayed()
        screen.host.apply {
            assertIsDisplayed()
            assertIsFocused()
        }
        captureErrorPreview(compose.onRoot(), "tv-banner.png")
        TvErrorRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.runOnIdle {
            assertEquals(1, hostClicks)
            assertEquals(emptyList<ErrorActionKind>(), errorActions)
        }
    }

    @Test
    fun fullScreenWithoutActionsRendersLocalCopyWithoutInventingButtons() {
        compose.setContent {
            QuizzTvTheme {
                QuizzTvErrorFullScreen(spec(ErrorSurface.BLOCKING).copy(actions = emptyList()), onAction = {})
            }
        }

        screen.text(copy(R.string.quizz_error_server_unavailable)).assertIsDisplayed()
        screen.text(copy(R.string.quizz_error_action_confirm_retry)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_action_go_back)).assertDoesNotExist()
    }

    @Test
    fun remoteBackReachesOwnerWithoutInvokingErrorAction() {
        var backEvents = 0
        val errorActions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            BackHandler { backEvents++ }
            QuizzTvTheme { QuizzTvErrorFullScreen(spec(ErrorSurface.BLOCKING), onAction = errorActions::add) }
        }

        TvErrorRemote.press(KeyEvent.KEYCODE_BACK)
        compose.runOnIdle {
            assertEquals(1, backEvents)
            assertEquals(emptyList<ErrorActionKind>(), errorActions)
        }
    }

    @Composable
    private fun FocusedHost(onClick: () -> Unit) {
        val focus = remember { FocusRequester() }
        QuizzTvButton(onClick = onClick, modifier = Modifier.focusRequester(focus)) { Text("Saved host content") }
        LaunchedEffect(focus) { focus.requestFocus() }
    }

    private fun copy(resource: Int): String = compose.activity.getString(resource)

    private fun spec(surface: ErrorSurface) = ErrorPresentationSpec(
        surface = surface,
        title = ErrorCopyKey.SERVER_UNAVAILABLE,
        actions = listOf(ErrorActionKind.CONFIRM_RETRY, ErrorActionKind.DISMISS),
    )
}
