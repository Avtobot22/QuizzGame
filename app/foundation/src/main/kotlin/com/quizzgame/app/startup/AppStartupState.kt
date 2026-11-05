package com.quizzgame.app.startup

import com.quizzgame.core.error.presentation.ErrorPresentationSpec

sealed interface AppStartupState {
    data object Idle : AppStartupState
    data object Loading : AppStartupState
    data object Ready : AppStartupState
    data object Closed : AppStartupState
    data class Failed(val error: ErrorPresentationSpec, val reason: AppStartupFailure) : AppStartupState {
        val configurationRequired: Boolean get() = reason == AppStartupFailure.NetworkConfiguration
    }
}
