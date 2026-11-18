package com.quizzgame.tv.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.tv.QuizzTvMessageScreen
import com.quizzgame.core.navigation.chrome.NavigationChromePolicy
import com.quizzgame.core.navigation.chrome.navigationChrome
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import com.quizzgame.feature.dashboard.tv.navigation.DashboardContext
import com.quizzgame.feature.dashboard.tv.navigation.DashboardTabRoute
import com.quizzgame.feature.dashboard.tv.ui.TvDashboardScreen
import com.quizzgame.feature.feed.tv.ui.TvFeedScreen
import com.quizzgame.feature.quizentry.tv.navigation.QuizEntryRoute
import com.quizzgame.feature.quizentry.tv.ui.TvQuizEntryScreen
import com.quizzgame.tv.R

@Composable
internal fun TvCatalogDashboard(onProfile: () -> Unit, onSignIn: () -> Boolean = { false }) {
    val graphs = remember(onSignIn) {
        setOf(
            FeatureEntries<DashboardContext> { scope, current ->
                scope.entry<DashboardTabRoute> { route ->
                    when (route.id) {
                        DashboardTabId.FEED -> {
                            TvFeedScreen(
                                { current.value.open(QuizEntryRoute(it)) },
                                current.value.requestFocus,
                                onSignIn = { onSignIn() },
                            )
                        }
                        DashboardTabId.LIBRARY, DashboardTabId.SETTINGS -> {
                            QuizzTvMessageScreen(
                                stringResource(R.string.catalog_section_title),
                                stringResource(R.string.catalog_section_placeholder),
                            )
                        }
                    }
                }
                scope.entry<QuizEntryRoute>(metadata = navigationChrome(NavigationChromePolicy.HIDDEN)) { route ->
                    TvQuizEntryScreen(route.entryId, current.value.back, onSignIn = { onSignIn() })
                }
            },
        )
    }
    TvDashboardScreen(onProfile, graphs, onSignIn = { onSignIn() })
}
