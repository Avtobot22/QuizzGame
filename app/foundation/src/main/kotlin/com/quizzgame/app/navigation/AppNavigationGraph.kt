package com.quizzgame.app.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.NavigationState
import com.quizzgame.core.navigation.decoratedEntries
import com.quizzgame.core.navigation.policy.DestinationAccess
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.registration.rememberFeatureEntryProvider
import com.quizzgame.core.navigation.rememberNavigationState
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.core.session.lifecycle.SessionState

data class AppNavigationEnvironment(
    val identity: String,
    val owner: String,
    val session: () -> SessionState,
    val isCurrent: () -> Boolean = { true },
)

data class AppNavigationConfiguration(
    val primary: NavKey,
    val splash: NavKey,
    val signIn: NavKey,
    val topLevels: List<NavKey> = listOf(primary),
    val policy: NavigationPolicy = NavigationPolicy {
        if (it == primary) DestinationAccess.PUBLIC else DestinationAccess.DENIED
    },
)

@Composable
fun <C> AppNavigationGraph(
    configuration: AppNavigationConfiguration,
    environment: AppNavigationEnvironment,
    initialDestination: NavKey,
    graphs: Set<FeatureEntries<C>>,
    context: (NavigationCoordinator) -> C,
    externalRequest: NavigationRequest? = null,
    onRequestHandled: (NavigationRequest) -> Unit = {},
    mainContent: @Composable (NavigationState, AppNavigator, List<NavEntry<NavKey>>, () -> Unit) -> Unit =
        { _, _, entries, onBack -> NavDisplay(entries = entries, onBack = onBack) },
) {
    val currentEnvironment = rememberUpdatedState(environment)
    val currentConfiguration = rememberUpdatedState(configuration)
    val state = rememberNavigationState(configuration.primary, configuration.topLevels, listOf(initialDestination))
    val checkpoint = rememberSerializable(stateSerializer = NavigationCheckpoint.serializer()) {
        mutableStateOf(NavigationCheckpoint())
    }
    val coordinator = remember(state, checkpoint) {
        NavigationCoordinator(
            AppNavigator(state),
            configuration.splash,
            configuration.signIn,
            session = { currentEnvironment.value.session() },
            policy = { currentConfiguration.value.policy.access(it) },
            checkpoint = checkpoint,
        )
    }
    LaunchedEffect(environment.identity) {
        coordinator.synchronize(
            environment.identity,
            environment.owner,
            requiresSignIn = initialDestination == configuration.signIn,
        )
    }
    if (coordinator.state.validatedIdentity != environment.identity || !environment.isCurrent()) return
    LaunchedEffect(externalRequest, environment.identity) {
        externalRequest?.let {
            coordinator.open(it)
            onRequestHandled(it)
        }
    }
    val activity = LocalActivity.current
    val entries = state.decoratedEntries(rememberFeatureEntryProvider(graphs, context(coordinator)))
    val onBack: () -> Unit = { if (!coordinator.back()) activity?.finish() }
    key(state.snapshot.generation, state.snapshot.flow.isEmpty()) {
        if (state.snapshot.flow.isNotEmpty()) {
            NavDisplay(modifier = Modifier.fillMaxSize(), entries = entries, onBack = onBack)
        } else {
            mainContent(state, coordinator.navigator, entries, onBack)
        }
    }
    val current = state.snapshot
    val isSecondaryRoot = current.flow.isEmpty() && current.active != current.primary && current.activeEntries.size == 1
    if (isSecondaryRoot || current.flow.lastOrNull() == configuration.signIn) {
        BackHandler { if (!coordinator.back()) activity?.finish() }
    }
}
