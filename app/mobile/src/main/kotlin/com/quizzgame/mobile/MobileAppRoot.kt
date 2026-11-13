package com.quizzgame.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.app.navigation.AppNavigationEnvironment
import com.quizzgame.app.presentation.AppEntryScreen
import com.quizzgame.app.presentation.AppEntryState
import com.quizzgame.app.presentation.AppRootTags
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorFullScreen
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.routes.common.SignInRoute
import com.quizzgame.feature.splash.mobile.navigation.MobileSplashRoute
import com.quizzgame.mobile.navigation.MobileNavigationContext
import com.quizzgame.mobile.navigation.MobileRootGraph

@Composable
internal fun MobileAppRoot(
    graphs: Set<FeatureEntries<MobileNavigationContext>>,
    viewModel: AppRootViewModel = hiltViewModel(),
    policies: Set<NavigationPolicy> = emptySet(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val externalRequest by viewModel.pendingNavigation.collectAsStateWithLifecycle()
    val entryState by viewModel.entryState.collectAsStateWithLifecycle()
    var roomCode by rememberSaveable { mutableStateOf("") }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            AppStartupState.Closed -> Unit
            AppStartupState.Idle, AppStartupState.Loading ->
                Text(stringResource(R.string.app_startup_loading), Modifier.testTag(AppRootTags.STARTUP_LOADING))
            is AppStartupState.Failed -> Column(Modifier.fillMaxSize().testTag(AppRootTags.STARTUP_FAILED)) {
                if (current.configurationRequired) Text(stringResource(R.string.app_network_configuration_required))
                QuizzMobileErrorFullScreen(current.error, viewModel::onAction, Modifier.weight(1f))
            }
            AppStartupState.Ready -> when (val entry = entryState) {
                AppEntryState.Waiting ->
                    Text(stringResource(R.string.app_startup_loading), Modifier.testTag(AppRootTags.STARTUP_LOADING))
                is AppEntryState.Blocked -> Box(Modifier.fillMaxSize().testTag(AppRootTags.ENTRY_FAILED)) {
                    QuizzMobileErrorFullScreen(entry.error, viewModel::onAction)
                }
                is AppEntryState.Ready -> {
                    val initialDestination = when (entry.screen) {
                        AppEntryScreen.SPLASH -> MobileSplashRoute
                        AppEntryScreen.SIGN_IN -> SignInRoute
                    }
                    MobileRootGraph(
                        graphs = graphs,
                        policies = policies,
                        externalRequest = externalRequest,
                        onRequestHandled = viewModel::navigationRequestHandled,
                        initialDestination = initialDestination,
                        isCurrentGraph = { viewModel.isCurrentEntry(entry) },
                        roomCode = roomCode,
                        onRoomCodeChange = { roomCode = it },
                        onLogout = viewModel::logout,
                        environment = AppNavigationEnvironment(
                            entry.identity,
                            entry.restorationOwner,
                            session = { viewModel.sessionState.value },
                            isCurrent = { viewModel.isCurrentEntry(entry) },
                        ),
                    )
                }
            }
        }
    }
}
