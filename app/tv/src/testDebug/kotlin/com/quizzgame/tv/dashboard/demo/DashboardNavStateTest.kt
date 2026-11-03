package com.quizzgame.tv.dashboard.demo

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardNavStateTest {
    @Test
    fun tabsRetainIndependentHistoryAndReselectDoesNotReset() {
        val detail = DashboardDestination.QuizDetail("42")
        val main = DashboardNavState().open(detail)
        val library = main.select(DashboardTabId.LIBRARY)
        val restored = library.select(DashboardTabId.MAIN)
        assertEquals(main, restored)
        assertEquals(main, restored.select(DashboardTabId.MAIN))
        assertEquals(main, main.open(detail))
        assertEquals(listOf(DashboardDestination.TabRoot(DashboardTabId.MAIN)), main.pop().stacks[DashboardTabId.MAIN])
        assertFalse(main.pop().canPop)
    }

    @Test
    fun removingTheSelectedTabSelectsTheFirstAllowedTabAndClearsClosedHistory() {
        val main = DashboardNavState().open(DashboardDestination.QuizDetail("private"))
        val updated = main.reconfigure(listOf(DashboardTabId.SETTINGS, DashboardTabId.LIBRARY))
        assertEquals(DashboardTabId.SETTINGS, updated.selectedTab)
        assertEquals(listOf(DashboardDestination.TabRoot(DashboardTabId.MAIN)), updated.stacks[DashboardTabId.MAIN])
        assertEquals(updated, updated.select(DashboardTabId.MAIN))
        assertNull(updated.reconfigure(emptyList()).selectedTab)
    }

    @Test
    fun saveableModelRoundTripsWithSelectedTabAndItsDetail() {
        val state = DashboardNavState().open(DashboardDestination.QuizDetail("42")).select(DashboardTabId.LIBRARY)
        assertEquals(state, Json.decodeFromString<DashboardNavState>(Json.encodeToString(state)))
    }
}
