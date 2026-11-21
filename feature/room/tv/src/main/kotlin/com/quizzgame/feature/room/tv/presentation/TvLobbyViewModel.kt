package com.quizzgame.feature.room.tv.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.usecase.ObserveRoomLobbyUseCase
import com.quizzgame.shared.room.domain.usecase.SendRoomCommandUseCase
import com.quizzgame.shared.room.presentation.RoomLobbyCommandError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel(assistedFactory = TvLobbyViewModel.Factory::class)
internal class TvLobbyViewModel @AssistedInject constructor(
    @Assisted id: UUID,
    private val observeRoomLobbyUseCase: ObserveRoomLobbyUseCase,
    private val sendRoomCommandUseCase: SendRoomCommandUseCase,
) : BaseViewModel<TvLobbyState, TvLobbyIntent, Nothing>() {

    private val roomId = RoomId(id)
    private val restarts = MutableStateFlow(0)

    override fun createInitialState() = TvLobbyState()

    override fun handleIntent(intent: TvLobbyIntent) {
        val current = state.value
        if (current.sending || current.exited || current.endReason != null) return
        when (intent) {
            is TvLobbyIntent.Command -> send(intent)
            TvLobbyIntent.Retry -> {
                val error = current.commandError
                if (error?.failure?.uncertain == true) send(error.command) else restarts.value++
            }
        }
    }

    suspend fun observe() {
        restarts.collectLatest { connect() }
    }

    private suspend fun connect() {
        if (state.value.endReason != null) return
        observeRoomLobbyUseCase(roomId).collect { result ->
            when (result) {
                is RoomResult.Failed -> updateState { it.copy(stream = RoomStreamState.Failed(result.failure)) }
                is RoomResult.Success -> {
                    val lobby = result.value
                    updateState { it.copy(stream = lobby.stream) }
                }
            }
        }
    }

    private fun send(intent: TvLobbyIntent.Command) {
        val command = when (intent) {
            is TvLobbyIntent.Kick -> RoomCommand.Kick(intent.participantId)
            TvLobbyIntent.Close   -> RoomCommand.Close
        }
        updateState { it.copy(sending = true, commandError = null) }
        viewModelScope.launch {
            val result = sendRoomCommandUseCase(roomId, command)
            val failure = (result as? RoomResult.Failed)?.failure
            updateState {
                it.copy(
                    sending = false,
                    commandError = failure?.let { RoomLobbyCommandError(intent, it) },
                    exited = intent == TvLobbyIntent.Close && result is RoomResult.Success,
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: UUID): TvLobbyViewModel
    }
}
