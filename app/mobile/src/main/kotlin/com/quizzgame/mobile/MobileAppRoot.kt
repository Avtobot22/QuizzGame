package com.quizzgame.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.core.error.ui.mobile.components.QuizzMobileErrorFullScreen
import com.quizzgame.mobile.navigation.MobileRootGraph

@Composable
internal fun MobileAppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            AppStartupState.Closed -> Unit
            AppStartupState.Idle, AppStartupState.Loading ->
                Text(stringResource(R.string.app_startup_loading), Modifier.testTag("app-startup-loading"))
            is AppStartupState.Failed -> Column(Modifier.fillMaxSize().testTag("app-startup-failed")) {
                if (current.configurationRequired) Text(stringResource(R.string.app_network_configuration_required))
                QuizzMobileErrorFullScreen(current.error, viewModel::onAction, Modifier.weight(1f))
            }
            AppStartupState.Ready -> MobileRootGraph(sessionState)
        }
    }
}
