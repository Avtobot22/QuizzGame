package com.quizzgame.feature.room.mobile.presentation

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
import com.quizzgame.shared.room.presentation.RoomLobbyCommandError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel(assistedFactory = MobileLobbyViewModel.Factory::class)
internal class MobileLobbyViewModel @AssistedInject constructor(
    @Assisted id: UUID,
    private val observeRoomLobbyUseCase: ObserveRoomLobbyUseCase,
    private val sendRoomCommandUseCase: SendRoomCommandUseCase,
    private val loadQuizEntryUseCase: LoadQuizEntryUseCase,
) : BaseViewModel<MobileLobbyState, MobileLobbyIntent, Nothing>() {

    private val roomId = RoomId(id)
    private val restarts = MutableStateFlow(0)
    private var loadedGameId: UUID? = null

    override fun createInitialState() = MobileLobbyState()

    override fun handleIntent(intent: MobileLobbyIntent) {
        val current = state.value
        if (current.sending || current.exited || current.endReason != null) return
        when (intent) {
            is MobileLobbyIntent.Command -> send(intent)
            MobileLobbyIntent.Retry -> {
                val error = current.commandError
                if (error?.failure?.uncertain == true) send(error.command) else restarts.value++
            }
        }
    }

    suspend fun observe(): Unit = coroutineScope {
        launch { observeGameInfo() }
        restarts.collectLatest { connect() }
    }

    private suspend fun observeGameInfo() {
        state.map { it.snapshot?.entry?.id }.distinctUntilChanged().collectLatest { id ->
            if (id == null || id == loadedGameId) return@collectLatest
            val entry = (loadQuizEntryUseCase(id.toString()) as? QuizEntryResult.Loaded)?.content
            loadedGameId = id
            val info = entry?.let {
                val description = it.description?.takeIf { text -> text.isNotBlank() && text != entry.title }
                val subtitle = it.subtitle?.takeIf { text -> text.isNotBlank() && text != entry.title }
                MobileLobbyGameInfo(
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

    private suspend fun connect() {
        if (state.value.endReason != null) return
        observeRoomLobbyUseCase(roomId).collect { result ->
            when (result) {
                is RoomResult.Failed -> updateState { it.copy(stream = RoomStreamState.Failed(result.failure)) }
                is RoomResult.Success -> {
                    val lobby = result.value
                    updateState { it.copy(stream = lobby.stream, self = lobby.self) }
                }
            }
        }
    }

    private fun send(intent: MobileLobbyIntent.Command) {
        val command = when (intent) {
            is MobileLobbyIntent.SetReady -> RoomCommand.Ready(intent.ready)
            MobileLobbyIntent.Leave        -> RoomCommand.Leave
        }
        updateState { it.copy(sending = true, commandError = null) }
        viewModelScope.launch {
            val result = sendRoomCommandUseCase(roomId, command)
            val failure = (result as? RoomResult.Failed)?.failure
            updateState {
                it.copy(
                    sending = false,
                    commandError = failure?.let { RoomLobbyCommandError(intent, it) },
                    exited = intent == MobileLobbyIntent.Leave && result is RoomResult.Success,
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: UUID): MobileLobbyViewModel
    }
}
