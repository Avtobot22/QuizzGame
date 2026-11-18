package com.quizzgame.tv.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.navigation.AppNavigationConfiguration
import com.quizzgame.app.navigation.AppNavigationEnvironment
import com.quizzgame.app.navigation.AppNavigationGraph
import com.quizzgame.app.navigation.AuthReturnTarget
import com.quizzgame.core.navigation.policy.DestinationAccess
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.account.tv.navigation.TvProfileRoute
import com.quizzgame.feature.splash.tv.navigation.TvSplashRoute
import com.quizzgame.tv.navigation.routes.DashboardRoute

@Composable
internal fun TvRootGraph(
    graphs: Set<FeatureEntries<TvNavigationContext>>,
    initialDestination: NavKey,
    isCurrentGraph: () -> Boolean,
    onLogout: () -> Unit,
    policies: Set<NavigationPolicy> = emptySet(),
    externalRequest: NavigationRequest? = null,
    onRequestHandled: (NavigationRequest) -> Unit = {},
    environment: AppNavigationEnvironment = AppNavigationEnvironment("test", "test", { SessionState.Guest }),
) {
    val activity = LocalActivity.current
    var openSettings by remember(environment.identity) { mutableStateOf(false) }
    AppNavigationGraph(
        configuration = AppNavigationConfiguration(
            DashboardRoute,
            TvSplashRoute,
            SignInRoute,
            policy = NavigationPolicy { destination ->
                if (destination == DashboardRoute || destination == TvProfileRoute) {
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
        onAuthReturn = { navigator, pending ->
            when (pending.target) {
                AuthReturnTarget.Profile -> {
                    navigator.navigate(TvProfileRoute)
                    environment.auth.acknowledge(pending.id)
                }
                AuthReturnTarget.Home -> environment.auth.acknowledge(pending.id)
                is AuthReturnTarget.QuizEntry -> Unit
            }
        },
        context = { coordinator ->
            TvNavigationContext(
                onContentReady = { isCurrentGraph() && coordinator.splashCompleted() },
                onSignIn = { isCurrentGraph() && coordinator.requireSignIn() },
                onRestartSplash = { isCurrentGraph() && coordinator.restartSplash() },
                onLogout = onLogout,
                onCancelAuth = { if (!coordinator.cancelAuthentication()) activity?.finish() },
                authRecovery = (environment.session() as? SessionState.Blocked)?.kind ==
                    CredentialUnavailableKind.REFRESH_UNCERTAIN,
                onAuthFor = { isCurrentGraph() && coordinator.requireSignIn(it) },
                onProfile = { coordinator.navigator.navigate(TvProfileRoute) },
                onSettings = {
                    openSettings = true
                    coordinator.navigator.back()
                },
                authenticated = environment.session() is SessionState.Authenticated,
                openSettings = openSettings,
                settingsHandled = { openSettings = false },
                authReturn = environment.auth.pending?.takeIf { pending ->
                    coordinator.state.admitted &&
                        pending.generation == (environment.session() as? SessionState.Authenticated)?.sessionGeneration
                },
                acknowledgeReturn = environment.auth.acknowledge,
            )
        },
    )
}
