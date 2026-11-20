package com.quizzgame.shared.room.domain.repository

import com.quizzgame.core.session.lifecycle.SessionState
import kotlinx.coroutines.flow.StateFlow

fun interface RoomAccountState {
    fun state(): StateFlow<SessionState>
}
