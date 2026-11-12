package com.quizzgame.core.presentation

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

private const val EVENT_BUFFER_CAPACITY = 64

abstract class BaseViewModel<State : Any, Intent : Any, Event : Any> : ViewModel() {

    private val _state = MutableStateFlow(createInitialState())
    private val eventChannel = Channel<Event>(capacity = EVENT_BUFFER_CAPACITY)

    val state: StateFlow<State> = _state.asStateFlow()

    val events: Flow<Event> = eventChannel.receiveAsFlow()

    @MainThread
    fun onIntent(intent: Intent) {
        handleIntent(intent)
    }

    @MainThread
    protected abstract fun handleIntent(intent: Intent)

    protected abstract fun createInitialState(): State

    protected fun updateState(reducer: (State) -> State) {
        _state.update(reducer)
    }

    protected suspend fun emitEvent(event: Event) {
        eventChannel.send(event)
    }
}
