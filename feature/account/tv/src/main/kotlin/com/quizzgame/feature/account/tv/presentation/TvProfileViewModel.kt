package com.quizzgame.feature.account.tv.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.splash.domain.usecase.ReadAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvProfileViewModel @Inject constructor(private val readAccountUseCase: ReadAccountUseCase) :
    BaseViewModel<TvProfileState, TvProfileIntent, Nothing>() {
    private var loading: Job? = null

    init { load() }

    override fun createInitialState(): TvProfileState = TvProfileState.Loading

    override fun handleIntent(intent: TvProfileIntent) { load() }

    private fun load() {
        if (loading?.isActive == true) return
        val previous = state.value as? TvProfileState.Loaded
        if (previous != null) updateState { previous.copy(refreshing = true) }
        loading = viewModelScope.launch {
            val profile = readAccountUseCase()
            updateState { TvProfileState.Loaded(profile) }
        }
    }
}
