package com.quizzgame.app.navigation

import com.quizzgame.core.session.lifecycle.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class PendingAuthReturn internal constructor(
    val id: UUID,
    val target: AuthReturnTarget,
    val generation: Long,
)

/** Держит возврат в памяти корневой ViewModel и привязывает его к подтверждённому поколению сессии. */
internal class AuthContinuation {
    private var requested: Request? = null
    private val delivery = MutableStateFlow<PendingAuthReturn?>(null)
    val ready = delivery.asStateFlow()

    fun begin(target: AuthReturnTarget, elapsed: Long) {
        requested = Request(UUID.randomUUID(), target, elapsed)
        delivery.value = null
    }

    fun sessionChanged(state: SessionState, elapsed: Long) {
        when (state) {
            is SessionState.Authenticated -> {
                val request = requested
                requested = null
                if (request != null && elapsed - request.elapsed <= RETURN_TTL_MILLIS) {
                    delivery.value = PendingAuthReturn(request.id, request.target, state.sessionGeneration)
                } else if (delivery.value?.generation != state.sessionGeneration) {
                    delivery.value = null
                }
            }
            SessionState.Expired, SessionState.Closed -> cancel()
            else -> Unit
        }
    }

    fun acknowledge(id: UUID) {
        if (delivery.value?.id == id) delivery.value = null
    }

    fun cancel() {
        requested = null
        delivery.value = null
    }

    private data class Request(val id: UUID, val target: AuthReturnTarget, val elapsed: Long)

    private companion object { const val RETURN_TTL_MILLIS = 15 * 60 * 1000L }
}
