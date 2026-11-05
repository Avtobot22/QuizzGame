package com.quizzgame.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.session.lifecycle.SessionOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppRootViewModel @Inject constructor(private val startup: AppStartup) : ViewModel() {
    val sessionState = startup.sessionState
    val state = startup.state
    private val mutableLogoutRevision = MutableStateFlow(0L)
    val logoutRevision = mutableLogoutRevision.asStateFlow()
    private var logoutJob: Job? = null

    init {
        viewModelScope.launch { startup.ensureStarted() }
    }

    fun onAction(action: ErrorActionKind) {
        if (action == ErrorActionKind.RETRY) {
            viewModelScope.launch {
                startup.retryInitialization()
            }
        }
    }

    fun logout() {
        if (logoutJob?.isActive == true) return
        logoutJob = viewModelScope.launch {
            if (startup.logout() == SessionOperation.Applied) {
                mutableLogoutRevision.value = Math.incrementExact(mutableLogoutRevision.value)
            }
        }
    }
}
