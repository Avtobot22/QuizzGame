package com.quizzgame.debug.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FixtureSignInResult { RESTART_REQUESTED }

/** Результат тестового экрана входа. */
class FixtureSignInViewModel : ViewModel() {
    private val pending = MutableStateFlow<FixtureSignInResult?>(null)
    val result = pending.asStateFlow()

    fun requestRestart() { pending.value = FixtureSignInResult.RESTART_REQUESTED }

    fun resultHandled(result: FixtureSignInResult) {
        pending.compareAndSet(result, null)
    }
}
