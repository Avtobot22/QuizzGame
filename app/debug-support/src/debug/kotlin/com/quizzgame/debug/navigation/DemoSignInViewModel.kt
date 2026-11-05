package com.quizzgame.debug.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DemoSignInResult { GUEST_ACCESS_ALLOWED }

/** Demonstrates VM result delivery; does not authenticate a real session. */
class DemoSignInViewModel : ViewModel() {
    private val pending = MutableStateFlow<DemoSignInResult?>(null)
    val result = pending.asStateFlow()

    fun continueAsGuest() { pending.value = DemoSignInResult.GUEST_ACCESS_ALLOWED }

    fun resultHandled(result: DemoSignInResult) {
        pending.compareAndSet(result, null)
    }
}
