package com.quizzgame.tv.navigation

import androidx.compose.animation.EnterExitState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlinx.coroutines.flow.first

/** Запрос выполняется только после attach, окончания перехода и допуска lifecycle. */
@Composable
internal fun TvNavigationFocus(requester: FocusRequester, attached: Boolean, requested: Boolean) {
    val owner = LocalLifecycleOwner.current
    val transition = LocalNavAnimatedContentScope.current.transition
    LaunchedEffect(requester, attached, requested, owner, transition) {
        if (attached && requested) {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                snapshotFlow { !transition.isRunning && transition.currentState == EnterExitState.Visible }.first { it }
                requester.requestFocus()
            }
        }
    }
}
