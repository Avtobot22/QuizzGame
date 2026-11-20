package com.quizzgame.mobile.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.navigation.AppNavigationConfiguration
import com.quizzgame.app.navigation.AppNavigationEnvironment
import com.quizzgame.app.navigation.AppNavigationGraph
import com.quizzgame.app.navigation.AuthReturnTarget
import com.quizzgame.app.navigation.NavigationCoordinator
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
import com.quizzgame.feature.account.mobile.navigation.ProfileRoute
import com.quizzgame.feature.feed.mobile.navigation.ExploreRoute
import com.quizzgame.feature.play.mobile.navigation.MobileJoinRoute
import com.quizzgame.feature.play.mobile.navigation.PlayRoute
import com.quizzgame.feature.quizentry.mobile.navigation.MobileQuizEntryRoute
import com.quizzgame.feature.room.mobile.navigation.MobileLobbyRoute
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
            policy = NavigationPolicy { mobileAccess(it, policies) },
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
            mobileNavigationContext(coordinator, environment, isCurrentGraph, onLogout) { activity?.finish() }
        },
    )
}

private fun mobileAccess(destination: NavKey, policies: Set<NavigationPolicy>): DestinationAccess = when (destination) {
    is MobileQuizEntryRoute, is MobileLobbyRoute, is MobileJoinRoute -> DestinationAccess.PUBLIC
    else -> if (MobileTopLevelItems.any { it.route == destination }) {
        DestinationAccess.PUBLIC
    } else {
        policies.map { it.access(destination) }.filter { it != DestinationAccess.DENIED }
            .distinct().singleOrNull() ?: DestinationAccess.DENIED
    }
}

private fun mobileNavigationContext(
    coordinator: NavigationCoordinator,
    environment: AppNavigationEnvironment,
    isCurrentGraph: () -> Boolean,
    onLogout: () -> Unit,
    finish: () -> Unit,
): MobileNavigationContext {
    fun navigate(destination: NavKey): Boolean {
        if (!isCurrentGraph()) return false
        coordinator.navigator.navigate(destination)
        return true
    }
    return MobileNavigationContext(
        onContentReady = { isCurrentGraph() && coordinator.splashCompleted() },
        onSignIn = { isCurrentGraph() && coordinator.requireSignIn() },
        onRestartSplash = { isCurrentGraph() && coordinator.restartSplash() },
        onLogout = onLogout,
        onCancelAuth = { if (!coordinator.cancelAuthentication()) finish() },
        authRecovery = (environment.session() as? SessionState.Blocked)?.kind ==
            CredentialUnavailableKind.REFRESH_UNCERTAIN,
        onAuthFor = { isCurrentGraph() && coordinator.requireSignIn(it) },
        onOpenEntry = { navigate(MobileQuizEntryRoute(it)) },
        onLobby = { navigate(MobileLobbyRoute(it)) },
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
}

private fun AppNavigator.deliverAuthReturn(pending: PendingAuthReturn) {
    when (val target = pending.target) {
        AuthReturnTarget.Home -> switchTopLevel(PlayRoute)
        AuthReturnTarget.Profile -> switchTopLevel(ProfileRoute)
        is AuthReturnTarget.JoinRoomDraft -> {
            switchTopLevel(PlayRoute)
            navigate(MobileJoinRoute(target.code, target.nickname))
        }
        is AuthReturnTarget.QuizEntry -> {
            switchTopLevel(ExploreRoute)
            navigate(MobileQuizEntryRoute(target.entryId))
        }
    }
}
