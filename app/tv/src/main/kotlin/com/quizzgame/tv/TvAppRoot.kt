package com.quizzgame.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.core.error.ui.tv.components.QuizzTvErrorFullScreen
import com.quizzgame.core.navigation.ProcessNavigationGuard
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.navigation.sessionNavigationEpoch
import com.quizzgame.tv.navigation.DashboardNavigationContext
import com.quizzgame.tv.navigation.TvNavigationContext
import com.quizzgame.tv.navigation.TvRootGraph

@Composable
internal fun TvAppRoot(
    graphs: Set<FeatureEntries<TvNavigationContext>>,
    dashboardGraphs: Set<FeatureEntries<DashboardNavigationContext>>,
    viewModel: AppRootViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val logoutRevision by viewModel.logoutRevision.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            AppStartupState.Closed -> Unit
            AppStartupState.Idle, AppStartupState.Loading ->
                Text(stringResource(R.string.app_startup_loading), Modifier.testTag("app-startup-loading"))
            is AppStartupState.Failed -> Column(Modifier.fillMaxSize().testTag("app-startup-failed")) {
                if (current.configurationRequired) Text(stringResource(R.string.app_network_configuration_required))
                QuizzTvErrorFullScreen(current.error, viewModel::onAction, Modifier.weight(1f))
            }
            AppStartupState.Ready -> key(
                ProcessNavigationGuard.processEpoch,
                sessionNavigationEpoch(sessionState),
                logoutRevision,
            ) {
                TvRootGraph(graphs, dashboardGraphs, sessionState, viewModel::logout)
            }
        }
    }
}
