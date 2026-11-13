package com.quizzgame.tv

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.navigation.chrome.NavigationChromePolicy
import com.quizzgame.core.navigation.chrome.navigationChrome
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.dashboard.demo.DashboardDestination
import com.quizzgame.tv.dashboard.demo.DashboardTabId
import com.quizzgame.tv.dashboard.demo.DemoDashboard
import com.quizzgame.tv.dashboard.demo.DemoDashboardEntries
import com.quizzgame.tv.dashboard.demo.DemoDashboardTags
import com.quizzgame.tv.dashboard.demo.DemoEntryViewModel
import com.quizzgame.tv.dashboard.demo.DemoQuizDetail
import com.quizzgame.tv.navigation.DashboardNavigationContext
import com.quizzgame.tv.navigation.TvNavigationTags
import org.junit.Rule
import org.junit.Test

class TvNavigationChromeTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val screen get() = AppShellScreen(compose)

    @Test
    fun fullscreenHidesTheMenuAndBackRestoresContentFocusThenMenuFocus() {
        val fullscreen = FeatureEntries<DashboardNavigationContext> { scope, context ->
            scope.entry<DashboardDestination.QuizDetail>(metadata = navigationChrome(NavigationChromePolicy.HIDDEN)) {
                val model = viewModel { DemoEntryViewModel() }
                DemoQuizDetail(it.quizId, context.value.back, model)
            }
        }
        compose.setContent {
            QuizzTvTheme { DemoDashboard({}, setOf(DemoDashboardEntries.tabs(), fullscreen)) }
        }
        screen.tag(DemoDashboardTags.tab(DashboardTabId.MAIN)).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        screen.tag(DemoDashboardTags.DETAIL).assertIsDisplayed()
        screen.tag(TvNavigationTags.NAVIGATION_BAR).assertDoesNotExist()
        screen.tag(DemoDashboardTags.INCREMENT).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(DemoDashboardTags.quiz(DashboardTabId.MAIN, 0))
        screen.tag(DemoDashboardTags.quiz(DashboardTabId.MAIN, 0)).assertIsFocused()
        compose.onAllNodesWithTag(TvNavigationTags.NAVIGATION_BAR).assertCountEquals(1)
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        screen.tag(DemoDashboardTags.tab(DashboardTabId.MAIN)).assertIsFocused()
    }
}
