package com.quizzgame.feature.play.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzgame.feature.play.mobile.presentation.MobilePlayViewModel

@Composable
internal fun MobilePlayScreen(modifier: Modifier = Modifier) {
    val viewModel: MobilePlayViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    MobilePlayContent(state, viewModel::onIntent, modifier)
}
