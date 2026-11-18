package com.quizzgame.feature.feed.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.quizzgame.core.navigation.effect.NavigationResultEffect
import com.quizzgame.feature.feed.mobile.presentation.MobileFeedIntent
import com.quizzgame.feature.feed.mobile.presentation.MobileFeedViewModel
import com.quizzgame.feature.feed.mobile.ui.paging.MobileFeedPagingEffects

@Composable
internal fun MobileFeedScreen(onOpenEntry: (String) -> Boolean, onSignIn: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: MobileFeedViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sections = viewModel.feed.collectAsLazyPagingItems()
    MobileFeedPagingEffects(viewModel.events, sections)
    NavigationResultEffect(state.destination, { viewModel.onIntent(MobileFeedIntent.NavigationHandled) }, onOpenEntry)

    MobileFeedStateContent(sections, viewModel::items, viewModel::onIntent, onSignIn, modifier)
}
