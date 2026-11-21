package com.quizzgame.shared.room.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.quizentry.domain.entity.QuizEntryResult
import com.quizzgame.shared.quizentry.domain.usecase.LoadQuizEntryUseCase
import com.quizzgame.shared.room.domain.entity.command.RoomCommand
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.domain.entity.session.RoomStreamState
import com.quizzgame.shared.room.domain.usecase.ObserveRoomLobbyUseCase
import com.quizzgame.shared.room.domain.usecase.SendRoomCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RoomLobbyViewModel @Inject constructor(
    private val observeRoomLobbyUseCase: ObserveRoomLobbyUseCase,
    private val sendRoomCommandUseCase: SendRoomCommandUseCase,
    private val loadQuizEntryUseCase: LoadQuizEntryUseCase,
) : BaseViewModel<RoomLobbyState, RoomLobbyIntent, Nothing>() {

    private val restarts = MutableStateFlow(0)
    private var lastCommand: RoomLobbyIntent.Send? = null
    private var loadedGameId: UUID? = null

    override fun createInitialState() = RoomLobbyState()

    override fun handleIntent(intent: RoomLobbyIntent) {
        when (intent) {
            is RoomLobbyIntent.Send      -> send(intent)

            RoomLobbyIntent.Retry        -> {
                val command = lastCommand
                if (command != null) send(command) else restarts.value++
            }
        }
    }

    suspend fun observe(id: RoomId) {
        restarts.collectLatest { connect(id) }
    }

    suspend fun observeWithGameInfo(id: RoomId): Unit = coroutineScope {
        launch { observeGameInfo() }
        observe(id)
    }

    private suspend fun observeGameInfo() {
        state.map { it.snapshot?.entry?.id }.distinctUntilChanged().collectLatest { id ->
            if (id == null || id == loadedGameId) return@collectLatest
            val entry = (loadQuizEntryUseCase(id.toString()) as? QuizEntryResult.Loaded)?.content
            loadedGameId = id
            val info = entry?.let {
                val description = it.description?.takeIf { text -> text.isNotBlank() && text != entry.title }
                val subtitle = it.subtitle?.takeIf { text -> text.isNotBlank() && text != entry.title }
                RoomLobbyGameInfo(
                    description = description ?: subtitle,
                    questionCount = it.questionCount,
                    durationSeconds = it.durationSeconds,
                )
            }
            updateState { current ->
                if (current.snapshot?.entry?.id == id) current.copy(gameInfo = info) else current
            }
        }
    }

    private suspend fun connect(id: RoomId) {
        observeRoomLobbyUseCase(id).collect { result ->
            when (result) {
                is RoomResult.Failed -> updateState { it.copy(stream = RoomStreamState.Failed(result.failure)) }
                is RoomResult.Success -> {
                    val lobby = result.value
                    updateState { it.copy(stream = lobby.stream, self = lobby.self) }
                }
            }
        }
    }

    private fun send(intent: RoomLobbyIntent.Send) {
        if (state.value.sending) return
        updateState { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            val result = sendRoomCommandUseCase(intent.id, intent.command)
            lastCommand = intent.takeIf { result is RoomResult.Failed && result.failure.uncertain }
            val exiting = intent.command == RoomCommand.Leave || intent.command == RoomCommand.Close
            updateState {
                it.copy(
                    sending = false,
                    error = (result as? RoomResult.Failed)?.failure,
                    exited = exiting && result is RoomResult.Success,
                )
            }
        }
    }
}
