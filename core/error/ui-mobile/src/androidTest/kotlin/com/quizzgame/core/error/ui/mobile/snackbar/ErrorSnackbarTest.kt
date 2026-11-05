package com.quizzgame.core.error.ui.mobile.snackbar

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.common.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class ErrorSnackbarTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nativeHostAnnouncesTheErrorAndReturnsTheSelectedAction() {
        val host = SnackbarHostState()
        var result: ErrorActionKind? = null
        compose.setContent {
            QuizzMobileTheme {
                Scaffold(snackbarHost = { SnackbarHost(host) }) { padding ->
                    Text("Screen content", Modifier.padding(padding))
                }
                LaunchedEffect(host) { result = host.showErrorSnackbar(spec(), compose.activity::getString) }
            }
        }
        compose.onNodeWithText(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        compose.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite),
            useUnmergedTree = true,
        ).assertExists()
        compose.onNodeWithText(copy(R.string.quizz_error_action_retry)).performClick()
        compose.onNodeWithText(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        compose.runOnIdle { assertEquals(ErrorActionKind.RETRY, result) }
    }

    @Test
    fun leavingTheOwningCompositionCancelsTheNativeSnackbar() {
        val host = SnackbarHostState()
        val visible = mutableStateOf(true)
        var returned = false
        compose.setContent {
            QuizzMobileTheme {
                if (visible.value) {
                    SnackbarHost(host)
                    LaunchedEffect(host) {
                        host.showErrorSnackbar(spec(), compose.activity::getString)
                        returned = true
                    }
                }
            }
        }
        compose.onNodeWithText(copy(R.string.quizz_error_connection_problem)).assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.onNodeWithText(copy(R.string.quizz_error_connection_problem)).assertDoesNotExist()
        compose.runOnIdle {
            assertNull(host.currentSnackbarData)
            assertEquals(false, returned)
        }
    }

    private fun copy(resource: Int): String = compose.activity.getString(resource)

    private fun spec() = ErrorPresentationSpec(
        ErrorSurface.TRANSIENT,
        ErrorCopyKey.CONNECTION_PROBLEM,
        actions = listOf(ErrorActionKind.RETRY, ErrorActionKind.DISMISS),
    )
}
