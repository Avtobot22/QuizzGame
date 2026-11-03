package com.quizzgame.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.app.startup.startupFailureState
import com.quizzgame.core.error.DefaultErrorClassifier
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.session.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppRootViewModel @Inject constructor(private val startup: AppStartup) : ViewModel() {
    val sessionState = startup.sessionState
    val state = combine(startup.state, sessionState) { state, session ->
        if (state == AppStartupState.Ready && session is SessionState.Unavailable) {
            startupFailureState(DefaultErrorClassifier().fromSession(session), storageRetry = true)
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), startup.state.value)

    init {
        viewModelScope.launch { startup.ensureStarted() }
    }

    fun onAction(action: ErrorActionKind) {
        if (action == ErrorActionKind.RETRY) {
            viewModelScope.launch {
                if (startup.state.value == AppStartupState.Ready) {
                    startup.awaitGraph().session.restoreStoredSession()
                } else {
                    startup.retryInitialization()
                }
            }
        }
    }
}
