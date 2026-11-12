package com.quizzgame.app.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.navigation.AppNavigator
import com.quizzgame.core.navigation.policy.DestinationAccess
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.request.NavigationRequest
import com.quizzgame.core.session.lifecycle.SessionState

class NavigationCoordinator(
    val navigator: AppNavigator,
    private val splash: NavKey,
    private val signIn: NavKey,
    private val session: () -> SessionState,
    private val policy: NavigationPolicy,
    private val checkpoint: MutableState<NavigationCheckpoint> = mutableStateOf(NavigationCheckpoint()),
) {
    val state: NavigationCheckpoint get() = checkpoint.value

    fun synchronize(identity: String, owner: String, requiresSignIn: Boolean) {
        if (state.validatedIdentity == identity) return
        val authCompleted = navigator.state.snapshot.flow.lastOrNull() == signIn &&
            session() is SessionState.Authenticated
        val sameOwner = state.owner == owner
        if (sameOwner) {
            navigator.showFlow(if (requiresSignIn) signIn else splash)
        } else {
            navigator.replaceRoot(if (requiresSignIn) signIn else splash)
        }
        checkpoint.value = state.copy(
            owner = owner,
            validatedIdentity = identity,
            admitted = false,
            pending = state.pending.takeIf { sameOwner || authCompleted },
            handledRequestId = state.handledRequestId.takeIf { sameOwner },
        )
    }

    fun open(request: NavigationRequest): Boolean {
        if (request.id == state.handledRequestId || request == state.pending) return true
        if (!validRequest(request)) return false
        checkpoint.value = state.copy(pending = request)
        if (!isAllowed(request.destination) || !isAllowed(request.topLevel)) {
            navigator.showFlow(signIn)
        } else if (state.admitted) {
            deliverPending()
        }
        return true
    }

    fun splashCompleted(): Boolean {
        if (session() !is SessionState.Authenticated && session() != SessionState.Guest) return false
        if (navigator.state.snapshot.flow.lastOrNull() != splash) return state.admitted
        val deniedHistory = navigator.state.snapshot.branches.any { branch ->
            branch.entries.any { !isAllowed(it) }
        }
        if (deniedHistory) {
            navigator.replaceRoot(navigator.state.snapshot.primary)
        } else {
            navigator.completeFlow()
        }
        checkpoint.value = state.copy(admitted = true)
        deliverPending()
        return true
    }

    fun requireSignIn(): Boolean {
        navigator.showFlow(signIn)
        return true
    }

    fun restartSplash(): Boolean {
        checkpoint.value = state.copy(admitted = false)
        navigator.showFlow(splash)
        return true
    }

    fun cancelAuthentication(): Boolean {
        checkpoint.value = state.copy(pending = null)
        return if (state.admitted) {
            navigator.completeFlow()
            true
        } else {
            false
        }
    }

    fun back(): Boolean = if (navigator.state.snapshot.flow.lastOrNull() == signIn) {
        cancelAuthentication()
    } else {
        navigator.back()
    }

    private fun validRequest(request: NavigationRequest): Boolean =
        request.destination != splash && request.destination != signIn &&
            navigator.state.snapshot.branches.any { it.root == request.topLevel } &&
            navigator.state.snapshot.branches.none { it.root == request.destination && it.root != request.topLevel } &&
            policy.access(request.destination) != DestinationAccess.DENIED &&
            policy.access(request.topLevel) != DestinationAccess.DENIED

    private fun isAllowed(destination: NavKey): Boolean = when (policy.access(destination)) {
        DestinationAccess.PUBLIC -> true
        DestinationAccess.AUTHENTICATED -> session() is SessionState.Authenticated
        DestinationAccess.DENIED -> false
    }

    private fun deliverPending() {
        val request = state.pending ?: return
        when {
            !validRequest(request) -> checkpoint.value = state.copy(pending = null)
            !isAllowed(request.destination) || !isAllowed(request.topLevel) -> navigator.showFlow(signIn)
            else -> {
                navigator.completeFlow()
                if (navigator.state.snapshot.active != request.topLevel) navigator.switchTopLevel(request.topLevel)
                if (request.destination != request.topLevel) navigator.navigate(request.destination)
                checkpoint.value = state.copy(pending = null, handledRequestId = request.id)
            }
        }
    }
}
