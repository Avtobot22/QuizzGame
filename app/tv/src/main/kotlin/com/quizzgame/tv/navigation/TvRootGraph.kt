package com.quizzgame.tv.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.navigation.AppNavigationConfiguration
import com.quizzgame.app.navigation.AppNavigationEnvironment
import com.quizzgame.app.navigation.AppNavigationGraph
import com.quizzgame.core.navigation.policy.DestinationAccess
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.splash.tv.navigation.TvSplashRoute
import com.quizzgame.tv.navigation.routes.DashboardRoute

@Composable
internal fun TvRootGraph(
    graphs: Set<FeatureEntries<TvNavigationContext>>,
    initialDestination: NavKey,
    isCurrentGraph: () -> Boolean,
    dashboardGraphs: Set<FeatureEntries<DashboardNavigationContext>>,
    onLogout: () -> Unit,
    policies: Set<NavigationPolicy> = emptySet(),
    externalRequest: NavigationRequest? = null,
    onRequestHandled: (NavigationRequest) -> Unit = {},
    environment: AppNavigationEnvironment = AppNavigationEnvironment("test", "test", { SessionState.Guest }),
) {
    AppNavigationGraph(
        configuration = AppNavigationConfiguration(
            DashboardRoute,
            TvSplashRoute,
            SignInRoute,
            policy = NavigationPolicy { destination ->
                if (destination == DashboardRoute) {
                    DestinationAccess.PUBLIC
                } else {
                    policies.map { it.access(destination) }.filter { it != DestinationAccess.DENIED }
                        .distinct().singleOrNull() ?: DestinationAccess.DENIED
                }
            },
        ),
        environment = environment,
        initialDestination = initialDestination,
        graphs = graphs,
        externalRequest = externalRequest,
        onRequestHandled = onRequestHandled,
        context = { coordinator ->
            TvNavigationContext(
                dashboardGraphs,
                onContentReady = { isCurrentGraph() && coordinator.splashCompleted() },
                onSignIn = { isCurrentGraph() && coordinator.requireSignIn() },
                onRestartSplash = { isCurrentGraph() && coordinator.restartSplash() },
                onLogout = onLogout,
            )
        },
    )
}
