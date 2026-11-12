package com.quizzgame.mobile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.SavedStateHandle
import com.quizzgame.app.presentation.AppRootTags
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupFailure
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.app.startup.GraphAccessResult
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.session.lifecycle.SessionOperation
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.storage.SessionStorageFailure
import com.quizzgame.mobile.navigation.MobileDemoTags
import com.quizzgame.mobile.navigation.MobileNavigationModule
import com.quizzgame.mobile.navigation.MobileNavigationTags
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import com.quizzgame.core.error.ui.common.R as ErrorR

class AppStartupUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val screen: AppShellScreen
        get() = AppShellScreen(compose)

    @Test
    fun blockingStartupErrorRetriesItsOwnerAndThenShowsSplash() {
        val startup = FailingAppStartup()
        val viewModel = AppRootViewModel(startup, SavedStateHandle())
        compose.setContent {
            QuizzMobileTheme {
                MobileAppRoot(
                    setOf(startupTestEntries(), MobileNavigationModule.contentEntries()),
                    viewModel,
                )
            }
        }
        screen.tag(AppRootTags.STARTUP_FAILED).assertIsDisplayed()
        screen.text(compose.activity.getString(ErrorR.string.quizz_error_action_retry)).performClick()
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.SPLASH).isDisplayed() }
        screen.tag(MobileNavigationTags.SPLASH).assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, startup.retries) }
    }

    @Test
    fun confirmedExpiryDropsThePreviousRootAndShowsSignIn() {
        val startup = FailingAppStartup().apply { state.value = AppStartupState.Ready }
        val viewModel = AppRootViewModel(startup, SavedStateHandle())
        compose.setContent {
            QuizzMobileTheme {
                MobileAppRoot(
                    setOf(startupTestEntries(), MobileNavigationModule.contentEntries()),
                    viewModel,
                )
            }
        }
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.SPLASH).isDisplayed() }
        screen.tag(MobileDemoTags.action(1)).performClick()
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.JOIN_ROOM).isDisplayed() }
        compose.runOnIdle { startup.sessionState.value = SessionState.Expired }
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.SIGN_IN).isDisplayed() }
        screen.tag(MobileNavigationTags.JOIN_ROOM).assertDoesNotExist()
    }

    @Test
    fun signInPlaceholderRestartsSplashWithoutGrantingContent() {
        val startup = FailingAppStartup().apply { state.value = AppStartupState.Ready }
        val viewModel = AppRootViewModel(startup, SavedStateHandle())
        compose.setContent {
            QuizzMobileTheme {
                MobileAppRoot(
                    setOf(startupTestEntries(), MobileNavigationModule.contentEntries()),
                    viewModel,
                )
            }
        }
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.SPLASH).isDisplayed() }
        screen.tag(MobileDemoTags.action(0)).performClick()
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.SIGN_IN).isDisplayed() }
        screen.tag(MobileDemoTags.action(0)).performClick()
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.SPLASH).isDisplayed() }
        screen.tag(MobileNavigationTags.JOIN_ROOM).assertDoesNotExist()
    }

    @Test
    fun closedSessionShowsAnEntryErrorWithoutCreatingANavigationGraph() {
        val startup = FailingAppStartup().apply {
            state.value = AppStartupState.Ready
            sessionState.value = SessionState.Closed
        }
        val viewModel = AppRootViewModel(startup, SavedStateHandle())
        compose.setContent {
            QuizzMobileTheme {
                MobileAppRoot(
                    setOf(startupTestEntries(), MobileNavigationModule.contentEntries()),
                    viewModel,
                )
            }
        }
        screen.tag(AppRootTags.ENTRY_FAILED).assertIsDisplayed()
        screen.tag(MobileNavigationTags.SPLASH).assertDoesNotExist()
        screen.tag(MobileNavigationTags.JOIN_ROOM).assertDoesNotExist()
    }

    @Test
    fun failedLogoutShowsStorageFailureWithoutPretendingTheUserSignedOut() {
        val startup = FailingAppStartup().apply {
            state.value = AppStartupState.Ready
            sessionState.value = SessionState.Authenticated(1)
            logoutResult = SessionOperation.Failed(SessionStorageFailure.WRITE)
        }
        val viewModel = AppRootViewModel(startup, SavedStateHandle())
        compose.setContent {
            QuizzMobileTheme {
                MobileAppRoot(
                    setOf(startupTestEntries(), MobileNavigationModule.contentEntries()),
                    viewModel,
                )
            }
        }
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.SPLASH).isDisplayed() }
        screen.tag(MobileDemoTags.action(1)).performClick()
        compose.waitUntil(5_000) { compose.onNodeWithTag(MobileNavigationTags.JOIN_ROOM).isDisplayed() }
        screen.tag(MobileDemoTags.action(0)).performClick()
        compose.waitUntil(5_000) { compose.onNodeWithTag(AppRootTags.STARTUP_FAILED).isDisplayed() }
        screen.tag(MobileNavigationTags.SIGN_IN).assertDoesNotExist()
        compose.runOnIdle { assertEquals(0L, viewModel.logoutRevision.value) }
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
            AppStartupFailure.Storage(SessionStorageFailure.READ),
        ),
    )
    override val sessionState = MutableStateFlow<SessionState>(SessionState.Guest)
    var retries = 0
    var logoutResult: SessionOperation = SessionOperation.Applied
    override suspend fun ensureStarted() = Unit
    override suspend fun retryInitialization() {
        retries++
        state.value = AppStartupState.Ready
    }
    override suspend fun accessGraph(): GraphAccessResult = error("UI must not request the application graph")
    override suspend fun logout(): SessionOperation = logoutResult.also { result ->
        if (result is SessionOperation.Failed) {
            sessionState.value = SessionState.Unavailable(result.reason, SessionState.Authenticated(1))
            state.value = AppStartupState.Failed(
                ErrorPresentationSpec(ErrorSurface.BLOCKING, ErrorCopyKey.STORAGE_PROBLEM),
                AppStartupFailure.Storage(result.reason),
            )
        }
    }
}
