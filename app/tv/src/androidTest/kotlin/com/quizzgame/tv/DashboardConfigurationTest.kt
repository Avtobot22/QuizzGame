package com.quizzgame.tv

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.tv.dashboard.fixture.DashboardTabDescriptor
import com.quizzgame.tv.dashboard.fixture.DashboardTabId
import com.quizzgame.tv.dashboard.fixture.FixtureDashboard
import com.quizzgame.tv.dashboard.fixture.FixtureDashboardEntries
import com.quizzgame.tv.dashboard.fixture.ui.tags.FixtureDashboardTags
import org.junit.Rule
import org.junit.Test

class DashboardConfigurationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun disabledTabsRemainVisibleRemovedTabsDisappearAndAnEmptyConfigurationBlocksContent() {
        var tabs by mutableStateOf(listOf(DashboardTabDescriptor("main"), DashboardTabDescriptor("library")))
        compose.setContent {
            QuizzTvTheme {
                FixtureDashboard(
                    onLogout = {},
                    graphs = setOf(FixtureDashboardEntries.tabs(), FixtureDashboardEntries.details()),
                    tabs = tabs,
                )
            }
        }
        compose.onNodeWithTag(FixtureDashboardTags.tabContent(DashboardTabId.MAIN)).assertIsDisplayed()
        compose.runOnIdle {
            tabs = listOf(DashboardTabDescriptor("main", false), DashboardTabDescriptor("settings"))
        }
        compose.onNodeWithTag(FixtureDashboardTags.tab(DashboardTabId.MAIN)).assertIsNotEnabled()
        compose.onNodeWithTag(FixtureDashboardTags.tab(DashboardTabId.LIBRARY)).assertDoesNotExist()
        compose.onNodeWithTag(FixtureDashboardTags.tabContent(DashboardTabId.SETTINGS)).assertIsDisplayed()
        compose.runOnIdle { tabs = emptyList() }
        compose.onNodeWithTag(FixtureDashboardTags.EMPTY).assertIsDisplayed()
        compose.onNodeWithTag(FixtureDashboardTags.tabContent(DashboardTabId.SETTINGS)).assertDoesNotExist()
    }
}
