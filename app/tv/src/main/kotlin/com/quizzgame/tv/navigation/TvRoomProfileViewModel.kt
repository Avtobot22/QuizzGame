package com.quizzgame.tv.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizzgame.shared.room.domain.entity.result.RoomResult
import com.quizzgame.shared.room.domain.usecase.ObserveActiveRoomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class TvRoomProfileViewModel @Inject constructor(
    observeActiveRoomUseCase: ObserveActiveRoomUseCase,
) : ViewModel() {
    val session = observeActiveRoomUseCase()
        .map { (it as? RoomResult.Success)?.value }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
}
