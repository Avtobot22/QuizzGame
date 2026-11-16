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
import com.quizzgame.tv.dashboard.fixture.DashboardDestination
import com.quizzgame.tv.dashboard.fixture.DashboardTabId
import com.quizzgame.tv.dashboard.fixture.FixtureDashboard
import com.quizzgame.tv.dashboard.fixture.FixtureDashboardEntries
import com.quizzgame.tv.dashboard.fixture.FixtureEntryViewModel
import com.quizzgame.tv.dashboard.fixture.FixtureQuizDetail
import com.quizzgame.tv.dashboard.fixture.ui.tags.FixtureDashboardTags
import com.quizzgame.tv.navigation.DashboardNavigationContext
import com.quizzgame.tv.ui.tags.TvNavigationTags
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
                val model = viewModel { FixtureEntryViewModel() }
                FixtureQuizDetail(it.quizId, context.value.back, model)
            }
        }
        compose.setContent {
            QuizzTvTheme { FixtureDashboard({}, setOf(FixtureDashboardEntries.tabs(), fullscreen)) }
        }
        screen.tag(FixtureDashboardTags.tab(DashboardTabId.MAIN)).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_DPAD_DOWN)
        TvRemote.press(KeyEvent.KEYCODE_DPAD_CENTER)
        screen.tag(FixtureDashboardTags.DETAIL).assertIsDisplayed()
        screen.tag(TvNavigationTags.NAVIGATION_BAR).assertDoesNotExist()
        screen.tag(FixtureDashboardTags.INCREMENT).assertIsFocused()
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        compose.awaitFocused(FixtureDashboardTags.quiz(DashboardTabId.MAIN, 0))
        screen.tag(FixtureDashboardTags.quiz(DashboardTabId.MAIN, 0)).assertIsFocused()
        compose.onAllNodesWithTag(TvNavigationTags.NAVIGATION_BAR).assertCountEquals(1)
        TvRemote.press(KeyEvent.KEYCODE_BACK)
        screen.tag(FixtureDashboardTags.tab(DashboardTabId.MAIN)).assertIsFocused()
    }
}
