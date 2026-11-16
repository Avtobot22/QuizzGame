package com.quizzgame.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.dashboard.fixture.FixtureDashboard
import com.quizzgame.tv.dashboard.fixture.FixtureDashboardEntries
import com.quizzgame.tv.navigation.TvNavigationContext
import com.quizzgame.tv.navigation.routes.DashboardRoute
import com.quizzgame.tv.ui.tags.TvNavigationTags

internal fun fixtureContentEntries(): FeatureEntries<TvNavigationContext> = FeatureEntries { scope, context ->
    scope.entry<DashboardRoute> {
        Box(Modifier.fillMaxSize().testTag(TvNavigationTags.DASHBOARD)) {
            FixtureDashboard(
                context.value.onLogout,
                setOf(FixtureDashboardEntries.tabs(), FixtureDashboardEntries.details()),
            )
        }
    }
}
