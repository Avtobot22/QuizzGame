package com.quizzgame.core.navigation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun <T : Any> NavigationResultEffect(
    result: T?,
    onHandled: (T) -> Unit,
    onNavigate: (T) -> Boolean,
) {
    val owner = LocalLifecycleOwner.current
    val navigate by rememberUpdatedState(onNavigate)
    val handled by rememberUpdatedState(onHandled)
    LaunchedEffect(result, owner) {
        if (result != null) {
            var consumed = false
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (!consumed) {
                    if (navigate(result)) {
                        consumed = true
                        handled(result)
                    }
                }
            }
        }
    }
}
