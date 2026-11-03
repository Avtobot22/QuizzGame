package com.quizzgame.app.startup

import com.quizzgame.core.error.ErrorPresentationSpec

sealed interface AppStartupState {
    data object Idle : AppStartupState
    data object Loading : AppStartupState
    data object Ready : AppStartupState
    data class Failed(val error: ErrorPresentationSpec, val configurationRequired: Boolean = false) : AppStartupState
}
