package com.quizzgame.feature.dashboard.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.feature.dashboard.tv.navigation.DashboardContext
import com.quizzgame.feature.dashboard.tv.presentation.TvDashboardViewModel

@Composable
fun TvDashboardScreen(onLogout: () -> Unit, graphs: Set<FeatureEntries<DashboardContext>>, onSignIn: () -> Unit = {}) {
    val viewModel: TvDashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    TvDashboardStateContent(state, viewModel::onIntent, graphs, onLogout, onSignIn)
}
