package com.quizzgame.feature.authprovider.mobile.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.authprovider.mobile.data.GoogleCredentialAdapter
import com.quizzgame.feature.authprovider.mobile.navigation.YandexAppLink
import com.quizzgame.feature.authprovider.mobile.presentation.MobileOAuthIntent
import com.quizzgame.feature.authprovider.mobile.presentation.MobileOAuthState
import com.quizzgame.feature.authprovider.mobile.presentation.MobileOAuthViewModel
import com.quizzgame.shared.auth.domain.entity.AuthMethod

@Composable
fun mobileOAuth(): MobileOAuthUi {
    val viewModel: MobileOAuthViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var methods by remember(context) { mutableStateOf(availableMethods(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { methods = availableMethods(context) }
    val status = when (state) {
        MobileOAuthState.Idle, is MobileOAuthState.Error -> MobileOAuthStatus.IDLE
        MobileOAuthState.Committed                       -> MobileOAuthStatus.COMPLETED
        else                                             -> MobileOAuthStatus.IN_PROGRESS
    }
    MobileOAuthEffects(state, viewModel::claimBrowserLaunch, viewModel::onIntent)
    return MobileOAuthUi(
        methods,
        status,
        (state as? MobileOAuthState.Error)?.failure,
        start = { viewModel.onIntent(MobileOAuthIntent.Start(it)) },
        cancel = { viewModel.onIntent(MobileOAuthIntent.Cancel) },
    )
}

private fun availableMethods(context: Context): Set<AuthMethod> = setOfNotNull(
    AuthMethod.GOOGLE.takeIf { GoogleCredentialAdapter.available(context) },
    AuthMethod.YANDEX.takeIf { YandexAppLink.available(context) },
)
