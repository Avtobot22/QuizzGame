package com.quizzgame.core.navigation.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed

/** Navigation 3 переводит экран в RESUMED после перехода; цель уже установлена композицией. */
@Composable
fun TvEntryFocus(requester: FocusRequester, requested: Boolean = true) {
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(requester, requested, owner) {
        if (requested) owner.lifecycle.withResumed { requester.requestFocus() }
    }
}
