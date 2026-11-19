package com.quizzgame.app.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizzgame.app.navigation.AuthContinuation
import com.quizzgame.app.navigation.AuthNavigation
import com.quizzgame.app.navigation.AuthReturnTarget
import com.quizzgame.app.navigation.PendingAuthReturn
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
import java.util.concurrent.TimeUnit
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

    private val authContinuation = AuthContinuation()
    val pendingAuthReturn = authContinuation.ready

    init {
        viewModelScope.launch { startup.ensureStarted() }
        viewModelScope.launch {
            sessionState.collect { authContinuation.sessionChanged(it, elapsedMillis()) }
        }
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

    fun authNavigation(pending: PendingAuthReturn?): AuthNavigation = AuthNavigation(
        pending,
        { target: AuthReturnTarget -> authContinuation.begin(target, elapsedMillis()) },
        authContinuation::cancel,
        authContinuation::acknowledge,
    )

    fun logout() {
        if (logoutJob?.isActive == true) return
        logoutJob = viewModelScope.launch {
            if (startup.logout() == SessionOperation.Applied) {
                authContinuation.cancel()
                savedStateHandle[NAVIGATION_REQUEST] = null
                navigationRequest.value = null
                val revision = Math.incrementExact(mutableLogoutRevision.value)
                savedStateHandle[LOGOUT_REVISION] = revision
                mutableLogoutRevision.value = revision
            }
        }
    }

    private fun elapsedMillis() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    private companion object {
        const val NAVIGATION_REQUEST = "navigation-request"
        const val LOGOUT_REVISION = "logout-revision"
    }
}
