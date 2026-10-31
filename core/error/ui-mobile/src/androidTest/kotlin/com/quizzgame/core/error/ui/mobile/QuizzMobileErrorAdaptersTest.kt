package com.quizzgame.core.error.ui.mobile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
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

class QuizzMobileErrorAdaptersTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: MobileErrorScreen
        get() = MobileErrorScreen(compose)

    @Test
    fun fullScreenWithoutActionsShowsOnlyLocalTitleAndOptionalMessage() {
        compose.setContent {
            QuizzMobileTheme {
                QuizzMobileErrorFullScreen(
                    spec(ErrorSurface.BLOCKING),
                    onAction = {},
                    modifier = Modifier.testTag("error"),
                )
            }
        }

        screen.error.assertIsDisplayed()
        screen.assertPanelHorizontallyCentered()
        screen.text(copy(R.string.quizz_error_server_unavailable)).assertIsDisplayed()
        screen.text(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        screen.text(copy(R.string.quizz_error_action_retry)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_action_go_back)).assertDoesNotExist()
        captureErrorPreview(compose.onRoot(), "mobile-blocking.png")
    }

    @Test
    fun bannerReportsOnlyTheSuppliedConfirmationIntent() {
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            QuizzMobileTheme {
                QuizzMobileErrorBanner(
                    spec(ErrorSurface.BANNER, listOf(ErrorActionKind.CONFIRM_RETRY)),
                    onAction = actions::add,
                )
            }
        }

        captureErrorPreview(compose.onRoot(), "mobile-banner-actions.png")
        screen.action(copy(R.string.quizz_error_action_confirm_retry)).performClick()
        screen.text(copy(R.string.quizz_error_action_retry)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_action_sign_in)).assertDoesNotExist()
        compose.runOnIdle { assertEquals(listOf(ErrorActionKind.CONFIRM_RETRY), actions) }
    }

    @Test
    fun ownerDisablingActionsPreventsAnotherPendingInvocation() {
        val enabled = mutableStateOf(true)
        val actions = mutableListOf<ErrorActionKind>()
        compose.setContent {
            QuizzMobileTheme {
                QuizzMobileErrorBanner(
                    spec(ErrorSurface.BANNER, listOf(ErrorActionKind.RETRY)),
                    onAction = {
                        actions += it
                        enabled.value = false
                    },
                    enabled = enabled.value,
                )
            }
        }

        screen.action(copy(R.string.quizz_error_action_retry)).performClick()
        screen.action(copy(R.string.quizz_error_action_retry)).apply {
            assertIsNotEnabled()
            performClick()
        }
        compose.runOnIdle { assertEquals(listOf(ErrorActionKind.RETRY), actions) }
    }

    @Test
    fun severalActionsStayAvailableWithoutInventingNavigation() {
        val actions = listOf(ErrorActionKind.RETRY, ErrorActionKind.DISMISS, ErrorActionKind.OPEN_SETTINGS)
        compose.setContent {
            QuizzMobileTheme {
                QuizzMobileErrorBanner(spec(ErrorSurface.BANNER, actions), onAction = {})
            }
        }

        listOf(
            R.string.quizz_error_action_retry,
            R.string.quizz_error_action_dismiss,
            R.string.quizz_error_action_open_settings,
        ).forEach { screen.action(copy(it)).assertIsDisplayed() }
        screen.text(copy(R.string.quizz_error_action_go_back)).assertDoesNotExist()
        screen.text(copy(R.string.quizz_error_action_exit)).assertDoesNotExist()
    }

    @Test
    fun showingInlineErrorLeavesOwnerInputIntact() {
        val showError = mutableStateOf(false)
        compose.setContent {
            QuizzMobileTheme {
                var draft by remember { mutableStateOf("") }
                Column {
                    TextField(value = draft, onValueChange = { draft = it })
                    if (showError.value) {
                        QuizzMobileErrorInline(spec(ErrorSurface.INLINE), onAction = {})
                    }
                }
            }
        }

        screen.input.performTextInput("Saved room draft")
        compose.runOnIdle { showError.value = true }
        screen.input.assertTextEquals("Saved room draft")
        screen.text(copy(R.string.quizz_error_server_unavailable)).assertIsDisplayed()
    }

    @Test
    fun showingAndDismissingBannerPreservesContentAndScrollPosition() {
        val showBanner = mutableStateOf(false)
        var savedScroll = 0
        var readScroll: () -> Int = { 0 }
        compose.setContent {
            QuizzMobileTheme {
                val scroll = rememberScrollState()
                readScroll = { scroll.value }
                Column {
                    Column(Modifier.height(160.dp).verticalScroll(scroll)) {
                        repeat(20) { Text("Saved quiz $it") }
                    }
                    if (showBanner.value) {
                        QuizzMobileErrorBanner(
                            spec(ErrorSurface.BANNER, listOf(ErrorActionKind.DISMISS)),
                            onAction = { showBanner.value = false },
                        )
                    }
                }
            }
        }

        screen.text("Saved quiz 19").performScrollTo()
        compose.runOnIdle {
            savedScroll = readScroll()
            showBanner.value = true
        }
        screen.text("Saved quiz 19").assertIsDisplayed()
        screen.action(copy(R.string.quizz_error_action_dismiss)).performClick()
        screen.text(copy(R.string.quizz_error_server_unavailable)).assertDoesNotExist()
        screen.text("Saved quiz 19").assertIsDisplayed()
        compose.runOnIdle { assertEquals(savedScroll, readScroll()) }
    }

    private fun copy(resource: Int): String = compose.activity.getString(resource)

    private fun spec(surface: ErrorSurface, actions: List<ErrorActionKind> = emptyList()) = ErrorPresentationSpec(
        surface = surface,
        title = ErrorCopyKey.SERVER_UNAVAILABLE,
        message = ErrorCopyKey.CONNECTION_PROBLEM,
        actions = actions,
    )
}
