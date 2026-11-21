package com.quizzgame.feature.room.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.quizzgame.feature.room.mobile.presentation.MobileLobbyViewModel
import com.quizzgame.shared.room.domain.entity.session.RoomId

@Composable
fun MobileLobbyScreen(roomId: String, onBack: () -> Unit) {
    val id = RoomId.parse(roomId)
    if (id == null) {
        LaunchedEffect(roomId) { onBack() }
        return
    }
    val model = hiltViewModel<MobileLobbyViewModel, MobileLobbyViewModel.Factory>(key = roomId) { it.create(id.value) }
    val state by model.state.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner, model) {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) { model.observe() }
    }
    LaunchedEffect(state.exited) { if (state.exited) onBack() }
    MobileLobbyContent(
        state,
        onBack,
        onIntent = model::onIntent,
    )
}
