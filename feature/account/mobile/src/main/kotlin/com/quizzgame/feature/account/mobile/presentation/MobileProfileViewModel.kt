package com.quizzgame.feature.account.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.splash.domain.usecase.ReadAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MobileProfileViewModel @Inject constructor(private val readAccountUseCase: ReadAccountUseCase) :
    BaseViewModel<MobileProfileState, MobileProfileIntent, Nothing>() {
    private var request: Job? = null

    init { load() }

    override fun createInitialState(): MobileProfileState = MobileProfileState.Loading

    override fun handleIntent(intent: MobileProfileIntent) { load() }

    private fun load() {
        if (request?.isActive == true) return
        val previous = state.value as? MobileProfileState.Ready
        if (previous != null) updateState { previous.copy(refreshing = true) }
        request = viewModelScope.launch {
            val profile = readAccountUseCase()
            updateState { MobileProfileState.Ready(profile) }
        }
    }
}
