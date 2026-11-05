package com.quizzgame.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.quizzgame.app.graph.AppGraph
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.session.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.quizzgame.core.error.ui.common.R as ErrorR

class AppStartupUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun blockingStartupErrorRetriesItsOwnerAndThenShowsSplash() {
        val startup = FailingAppStartup()
        val viewModel = AppRootViewModel(startup)
        compose.setContent { QuizzTvTheme { TvAppRoot(viewModel) } }
        compose.onNodeWithTag("app-startup-failed").assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(ErrorR.string.quizz_error_action_retry)).assertIsDisplayed()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.waitUntil(5_000) { compose.onNodeWithTag("splash").isDisplayed() }
        compose.onNodeWithTag("splash").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, startup.retries) }
    }

    @Test
    fun confirmedExpiryDropsThePreviousRootAndShowsSignIn() {
        val startup = FailingAppStartup().apply { state.value = AppStartupState.Ready }
        val viewModel = AppRootViewModel(startup)
        compose.setContent { QuizzTvTheme { TvAppRoot(viewModel) } }
        compose.waitUntil(5_000) { compose.onNodeWithTag("splash").isDisplayed() }
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.waitUntil(5_000) { compose.onNodeWithTag("signin").isDisplayed() }
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.waitUntil(5_000) { compose.onNodeWithTag("dashboard").isDisplayed() }
        compose.runOnIdle { startup.sessionState.value = SessionState.Expired }
        compose.waitUntil(5_000) { compose.onNodeWithTag("signin").isDisplayed() }
        compose.onNodeWithTag("dashboard").assertDoesNotExist()
    }
}

private class FailingAppStartup : AppStartup {
    override val state = MutableStateFlow<AppStartupState>(
        AppStartupState.Failed(
            ErrorPresentationSpec(
                ErrorSurface.BLOCKING,
                ErrorCopyKey.STORAGE_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY),
            ),
        ),
    )
    override val sessionState = MutableStateFlow<SessionState>(SessionState.Guest)
    var retries = 0
    override suspend fun ensureStarted() = Unit
    override suspend fun retryInitialization() {
        retries++
        state.value = AppStartupState.Ready
    }
    override suspend fun awaitGraph(): AppGraph = error("UI must not request the application graph")
}
