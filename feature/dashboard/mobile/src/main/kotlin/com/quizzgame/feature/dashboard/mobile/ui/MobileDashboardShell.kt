package com.quizzgame.feature.dashboard.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.dashboard.mobile.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.mobile.presentation.MobileDashboardViewModel

/** Конфигурация существующего app-host; собственных стеков или NavDisplay здесь нет. */
@Composable
fun MobileDashboardShell(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Dashboard) -> Unit,
) {
    val viewModel: MobileDashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    MobileDashboardContent(state, viewModel::onIntent, onSignIn, modifier, content)
}
