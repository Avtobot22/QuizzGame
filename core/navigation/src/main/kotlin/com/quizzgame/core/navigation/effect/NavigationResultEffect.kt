package com.quizzgame.core.navigation.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun <T : Any> NavigationResultEffect(
    result: T?,
    onHandled: (T) -> Unit,
    onNavigate: (T) -> Unit,
) {
    val owner = LocalLifecycleOwner.current
    val navigate = rememberUpdatedState(onNavigate)
    val handled = rememberUpdatedState(onHandled)
    LaunchedEffect(result, owner) {
        if (result != null) {
            var consumed = false
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (!consumed) {
                    navigate.value(result)
                    consumed = true
                    handled.value(result)
                }
            }
        }
    }
}
