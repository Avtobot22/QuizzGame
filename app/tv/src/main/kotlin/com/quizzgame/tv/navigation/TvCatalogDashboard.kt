package com.quizzgame.tv.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.quizzgame.app.navigation.AuthReturnTarget
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
import com.quizzgame.feature.room.tv.navigation.TvLobbyRoute
import com.quizzgame.feature.room.tv.ui.TvLobbyScreen
import com.quizzgame.tv.R

@Composable
internal fun TvCatalogDashboard(navigation: TvNavigationContext) {
    val latest = rememberUpdatedState(navigation)
    val graphs = remember {
        setOf(
            FeatureEntries<DashboardContext> { scope, current ->
                scope.entry<DashboardTabRoute> { route ->
                    val pending = latest.value.authReturn
                    val quiz = pending?.target as? AuthReturnTarget.QuizEntry
                    LaunchedEffect(pending?.id) {
                        if (quiz != null && current.value.openInFeed(QuizEntryRoute(quiz.entryId))) {
                            latest.value.acknowledgeReturn(pending.id)
                        }
                    }
                    val settingsRequested = latest.value.openSettings
                    LaunchedEffect(settingsRequested) {
                        if (settingsRequested) {
                            current.value.openSettings()
                            latest.value.settingsHandled()
                        }
                    }
                    when (route.id) {
                        DashboardTabId.FEED -> {
                            TvFeedScreen(
                                { current.value.open(QuizEntryRoute(it)) },
                                current.value.requestFocus,
                                onSignIn = { latest.value.onSignIn() },
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
                    TvQuizEntryScreen(
                        route.entryId,
                        current.value.back,
                        onSignIn = { latest.value.onAuthFor(AuthReturnTarget.QuizEntry(route.entryId)) },
                        authenticated = latest.value.authenticated,
                        onLobby = { current.value.open(TvLobbyRoute(it)) },
                    )
                }
                scope.entry<TvLobbyRoute>(metadata = navigationChrome(NavigationChromePolicy.HIDDEN)) { route ->
                    TvLobbyScreen(route.roomId, current.value.back)
                }
            },
        )
    }
    TvDashboardScreen(navigation.onProfile, graphs, onSignIn = { latest.value.onSignIn() })
}
