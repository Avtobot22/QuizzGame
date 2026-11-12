package com.quizzgame.debug.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DemoSignInResult { RESTART_REQUESTED }

/** Demonstrates VM result delivery; does not authenticate a real session. */
class DemoSignInViewModel : ViewModel() {
    private val pending = MutableStateFlow<DemoSignInResult?>(null)
    val result = pending.asStateFlow()

    fun requestRestart() { pending.value = DemoSignInResult.RESTART_REQUESTED }

    fun resultHandled(result: DemoSignInResult) {
        pending.compareAndSet(result, null)
    }
}
