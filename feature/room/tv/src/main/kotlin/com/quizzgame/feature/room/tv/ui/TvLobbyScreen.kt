package com.quizzgame.feature.room.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.quizzgame.shared.room.domain.entity.session.RoomId
import com.quizzgame.shared.room.presentation.RoomLobbyIntent
import com.quizzgame.shared.room.presentation.RoomLobbyViewModel

@Composable
fun TvLobbyScreen(roomId: String, onBack: () -> Unit) {
    val id = RoomId.parse(roomId)
    val model: RoomLobbyViewModel = hiltViewModel(key = roomId)
    val state by model.state.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(id, owner, model) {
        if (id == null) onBack() else owner.repeatOnLifecycle(Lifecycle.State.STARTED) { model.observe(id) }
    }
    LaunchedEffect(state.exited) { if (state.exited) onBack() }
    TvLobbyContent(
        state,
        onBack,
        onCommand = { command -> if (id != null) model.onIntent(RoomLobbyIntent.Send(id, command)) },
        onRetry = { model.onIntent(RoomLobbyIntent.Retry) },
    )
}
