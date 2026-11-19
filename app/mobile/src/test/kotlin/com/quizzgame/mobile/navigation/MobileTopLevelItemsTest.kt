package com.quizzgame.mobile.navigation

import com.quizzgame.feature.account.mobile.navigation.ProfileRoute
import com.quizzgame.feature.dashboard.mobile.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.mobile.domain.entity.DashboardTab
import com.quizzgame.feature.dashboard.mobile.domain.entity.MobileTabId
import com.quizzgame.feature.play.mobile.navigation.PlayRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class MobileTopLevelItemsTest {
    @Test
    fun profileRemainsAvailableWhenDashboardDoesNotSendIt() {
        val dashboard = Dashboard(listOf(DashboardTab(MobileTabId.PLAY)))
        assertEquals(listOf(PlayRoute, ProfileRoute), dashboard.navigationItems().map { it.route })
    }

    @Test
    fun profileUsesServerAppearanceWhenItIsPresent() {
        val dashboard = Dashboard(listOf(DashboardTab(MobileTabId.PROFILE, title = "My profile")))
        val item = dashboard.navigationItems().single()
        assertEquals(ProfileRoute, item.route)
        assertEquals("My profile", item.title)
    }
}
