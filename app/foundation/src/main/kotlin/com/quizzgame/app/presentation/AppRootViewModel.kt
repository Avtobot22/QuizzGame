package com.quizzgame.app.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizzgame.app.startup.AppStartup
import com.quizzgame.app.startup.AppStartupState
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.core.session.lifecycle.SessionOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class AppRootViewModel @Inject constructor(
    private val startup: AppStartup,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val sessionState = startup.sessionState
    val state = startup.state
    private val mutableLogoutRevision = MutableStateFlow(savedStateHandle.get<Long>(LOGOUT_REVISION) ?: 0L)
    val logoutRevision = mutableLogoutRevision.asStateFlow()
    val entryState = combine(sessionState, logoutRevision) { session, revision ->
        appEntryState(session, revision)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        appEntryState(sessionState.value, logoutRevision.value),
    )
    private var logoutJob: Job? = null
    private val navigationRequest = MutableStateFlow(
        savedStateHandle.get<String>(NAVIGATION_REQUEST)?.let { Json.decodeFromString<NavigationRequest>(it) },
    )
    val pendingNavigation = navigationRequest.asStateFlow()

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

    fun isCurrentEntry(entry: AppEntryState.Ready): Boolean =
        startup.state.value == AppStartupState.Ready &&
            entry == appEntryState(sessionState.value, logoutRevision.value)

    fun requestNavigation(request: NavigationRequest) {
        savedStateHandle[NAVIGATION_REQUEST] = Json.encodeToString(request)
        navigationRequest.value = request
    }

    fun navigationRequestHandled(request: NavigationRequest) {
        if (navigationRequest.value == request) {
            savedStateHandle[NAVIGATION_REQUEST] = null
            navigationRequest.value = null
        }
    }

    fun logout() {
        if (logoutJob?.isActive == true) return
        logoutJob = viewModelScope.launch {
            if (startup.logout() == SessionOperation.Applied) {
                savedStateHandle[NAVIGATION_REQUEST] = null
                navigationRequest.value = null
                val revision = Math.incrementExact(mutableLogoutRevision.value)
                savedStateHandle[LOGOUT_REVISION] = revision
                mutableLogoutRevision.value = revision
            }
        }
    }

    private companion object {
        const val NAVIGATION_REQUEST = "navigation-request"
        const val LOGOUT_REVISION = "logout-revision"
    }
}
