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
import com.quizzgame.tv.dashboard.demo.DemoDashboard
import com.quizzgame.tv.dashboard.demo.DemoDashboardEntries
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
        compose.onNodeWithTag("tab-content-MAIN").assertIsDisplayed()
        compose.runOnIdle {
            tabs = listOf(DashboardTabDescriptor("main", false), DashboardTabDescriptor("settings"))
        }
        compose.onNodeWithTag("tab-MAIN").assertIsNotEnabled()
        compose.onNodeWithTag("tab-LIBRARY").assertDoesNotExist()
        compose.onNodeWithTag("tab-content-SETTINGS").assertIsDisplayed()
        compose.runOnIdle { tabs = emptyList() }
        compose.onNodeWithTag("dashboard-empty").assertIsDisplayed()
        compose.onNodeWithTag("tab-content-SETTINGS").assertDoesNotExist()
    }
}
