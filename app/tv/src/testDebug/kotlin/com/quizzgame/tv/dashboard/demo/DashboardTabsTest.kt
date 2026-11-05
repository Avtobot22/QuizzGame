package com.quizzgame.tv.dashboard.demo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardTabsTest {
    @Test
    fun selectionRetainsAnEnabledTabAndFallsBackWhenTheTabIsRemovedOrDisabled() {
        val tabs =
            resolveDashboardTabs(listOf(DashboardTabDescriptor("settings"), DashboardTabDescriptor("main", false)))
        assertEquals(DashboardTabId.SETTINGS, selectedDashboardTab(DashboardTabId.SETTINGS, tabs))
        assertEquals(DashboardTabId.SETTINGS, selectedDashboardTab(DashboardTabId.MAIN, tabs))
        assertEquals(DashboardTabId.SETTINGS, selectedDashboardTab(DashboardTabId.LIBRARY, tabs))
        assertNull(selectedDashboardTab(DashboardTabId.MAIN, tabs.filter { !it.isEnabled }))
        assertNull(selectedDashboardTab(null, emptyList()))
    }

    @Test
    fun unsupportedAndDuplicateTabsAreSkippedWithoutChangingServerOrder() {
        val tabs = resolveDashboardTabs(
            listOf(
                DashboardTabDescriptor("unknown"),
                DashboardTabDescriptor("settings", false),
                DashboardTabDescriptor("main"),
                DashboardTabDescriptor("settings"),
            ),
        )
        assertEquals(listOf(DashboardTab(DashboardTabId.SETTINGS, false), DashboardTab(DashboardTabId.MAIN)), tabs)
    }
}
