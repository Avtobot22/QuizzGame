package com.quizzgame.tv.dashboard.fixture

import com.quizzgame.core.navigation.chrome.NavigationChromePolicy
import com.quizzgame.core.navigation.chrome.navigationChrome
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.navigation.DashboardNavigationContext

internal object FixtureDashboardEntries {
    fun tabs(): FeatureEntries<DashboardNavigationContext> = FeatureEntries { scope, context ->
        scope.entry<DashboardDestination.TabRoot> { route ->
            val current = context.value
            FixtureTabContent(route.tab, current.requestFocus, current.open)
        }
    }

    fun details(): FeatureEntries<DashboardNavigationContext> = FeatureEntries { scope, context ->
        scope.entry<DashboardDestination.QuizDetail>(
            metadata = navigationChrome(
                NavigationChromePolicy.VISIBLE,
            ),
        ) { route -> FixtureQuizDetail(route.quizId, context.value.back) }
    }
}
