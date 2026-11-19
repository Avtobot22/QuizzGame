package com.quizzgame.mobile.navigation

import androidx.navigation3.runtime.NavKey
import com.quizzgame.feature.account.mobile.navigation.ProfileRoute
import com.quizzgame.feature.dashboard.mobile.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.mobile.domain.entity.MobileTabId
import com.quizzgame.feature.dashboard.mobile.navigation.LibraryRoute
import com.quizzgame.feature.feed.mobile.navigation.ExploreRoute
import com.quizzgame.feature.play.mobile.navigation.PlayRoute
import com.quizzgame.mobile.R
import com.quizzgame.core.ui.R as UiR

internal val MobileTopLevelItems = listOf(
    MobileTopLevelItem(PlayRoute, R.string.navigation_play, UiR.drawable.ic_quizz_room),
    MobileTopLevelItem(ExploreRoute, R.string.navigation_explore, UiR.drawable.ic_quizz_quiz),
    MobileTopLevelItem(LibraryRoute, R.string.navigation_library, UiR.drawable.ic_quizz_library),
    MobileTopLevelItem(ProfileRoute, R.string.navigation_profile, UiR.drawable.ic_quizz_profile),
)
internal fun MobileTabId.route(): NavKey = when (this) {
    MobileTabId.PLAY -> PlayRoute
    MobileTabId.EXPLORE -> ExploreRoute
    MobileTabId.LIBRARY -> LibraryRoute
    MobileTabId.PROFILE -> ProfileRoute
}
internal fun Dashboard.navigationItems(): List<MobileTopLevelItem> {
    val items = tabs.map { tab ->
        MobileTopLevelItems.first { it.route == tab.id.route() }.copy(
            title = tab.title,
            media = tab.icon,
            badge = tab.badge,
        )
    }
    return if (tabs.any { it.id == MobileTabId.PROFILE }) items else items + MobileTopLevelItems.last()
}
