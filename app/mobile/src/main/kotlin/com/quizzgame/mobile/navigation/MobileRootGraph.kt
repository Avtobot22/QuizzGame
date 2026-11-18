package com.quizzgame.mobile.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.navigation.AppNavigationConfiguration
import com.quizzgame.app.navigation.AppNavigationEnvironment
import com.quizzgame.app.navigation.AppNavigationGraph
import com.quizzgame.app.navigation.AuthReturnTarget
import com.quizzgame.app.navigation.PendingAuthReturn
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationState
import com.quizzgame.core.navigation.policy.DestinationAccess
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.feature.dashboard.mobile.navigation.ProfileRoute
import com.quizzgame.feature.feed.mobile.navigation.ExploreRoute
import com.quizzgame.feature.play.mobile.navigation.PlayRoute
import com.quizzgame.feature.quizentry.mobile.navigation.MobileQuizEntryRoute
import com.quizzgame.feature.splash.mobile.navigation.MobileSplashRoute

@Composable
internal fun MobileRootGraph(
    graphs: Set<FeatureEntries<MobileNavigationContext>>,
    initialDestination: NavKey,
    isCurrentGraph: () -> Boolean,
    onLogout: () -> Unit,
    policies: Set<NavigationPolicy> = emptySet(),
    mainContent: @Composable (NavigationState, AppNavigator, List<NavEntry<NavKey>>, () -> Unit) -> Unit =
        { state, navigator, entries, onBack -> MobileMainNavigationHost(state, navigator, entries, onBack) },
    externalRequest: NavigationRequest? = null,
    onRequestHandled: (NavigationRequest) -> Unit = {},
    environment: AppNavigationEnvironment = AppNavigationEnvironment("test", "test", { SessionState.Guest }),
) {
    val activity = LocalActivity.current
    AppNavigationGraph(
        configuration = AppNavigationConfiguration(
            PlayRoute,
            MobileSplashRoute,
            SignInRoute,
            topLevels = MobileTopLevelItems.map { it.route },
            policy = NavigationPolicy { destination ->
                if (MobileTopLevelItems.any { it.route == destination } || destination is MobileQuizEntryRoute) {
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
        mainContent = mainContent,
        onAuthReturn = { navigator, pending ->
            navigator.deliverAuthReturn(pending)
            environment.auth.acknowledge(pending.id)
        },
        context = { coordinator ->
            MobileNavigationContext(
                onContentReady = { isCurrentGraph() && coordinator.splashCompleted() },
                onSignIn = { isCurrentGraph() && coordinator.requireSignIn() },
                onRestartSplash = { isCurrentGraph() && coordinator.restartSplash() },
                onLogout = onLogout,
                onCancelAuth = { if (!coordinator.cancelAuthentication()) activity?.finish() },
                authRecovery = (environment.session() as? SessionState.Blocked)?.kind ==
                    CredentialUnavailableKind.REFRESH_UNCERTAIN,
                onAuthFor = { isCurrentGraph() && coordinator.requireSignIn(it) },
                onOpenEntry = { id ->
                    if (isCurrentGraph()) {
                        coordinator.navigator.navigate(MobileQuizEntryRoute(id))
                        true
                    } else {
                        false
                    }
                },
                onPlay = {
                    if (isCurrentGraph()) {
                        coordinator.navigator.switchTopLevel(PlayRoute)
                        true
                    } else {
                        false
                    }
                },
                onBack = { coordinator.back() },
            )
        },
    )
}

private fun AppNavigator.deliverAuthReturn(pending: PendingAuthReturn) {
    when (val target = pending.target) {
        AuthReturnTarget.Home -> switchTopLevel(PlayRoute)
        AuthReturnTarget.Profile -> switchTopLevel(ProfileRoute)
        is AuthReturnTarget.QuizEntry -> {
            switchTopLevel(ExploreRoute)
            navigate(MobileQuizEntryRoute(target.entryId))
        }
    }
}
