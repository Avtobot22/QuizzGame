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
import com.quizzgame.tv.dashboard.demo.DashboardTabDescriptor
import com.quizzgame.tv.dashboard.demo.DashboardTabId
import com.quizzgame.tv.dashboard.demo.DemoDashboard
import com.quizzgame.tv.dashboard.demo.DemoDashboardEntries
import com.quizzgame.tv.dashboard.demo.DemoDashboardTags
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
                DemoDashboard(
                    onLogout = {},
                    graphs = setOf(DemoDashboardEntries.tabs(), DemoDashboardEntries.details()),
                    tabs = tabs,
                )
            }
        }
        compose.onNodeWithTag(DemoDashboardTags.tabContent(DashboardTabId.MAIN)).assertIsDisplayed()
        compose.runOnIdle {
            tabs = listOf(DashboardTabDescriptor("main", false), DashboardTabDescriptor("settings"))
        }
        compose.onNodeWithTag(DemoDashboardTags.tab(DashboardTabId.MAIN)).assertIsNotEnabled()
        compose.onNodeWithTag(DemoDashboardTags.tab(DashboardTabId.LIBRARY)).assertDoesNotExist()
        compose.onNodeWithTag(DemoDashboardTags.tabContent(DashboardTabId.SETTINGS)).assertIsDisplayed()
        compose.runOnIdle { tabs = emptyList() }
        compose.onNodeWithTag(DemoDashboardTags.EMPTY).assertIsDisplayed()
        compose.onNodeWithTag(DemoDashboardTags.tabContent(DashboardTabId.SETTINGS)).assertDoesNotExist()
    }
}
