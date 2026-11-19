package com.quizzgame.feature.dashboard.mobile.presentation

import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.feature.dashboard.mobile.domain.entity.Account
import com.quizzgame.feature.dashboard.mobile.domain.usecase.LoadAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MobileAccountViewModel @Inject constructor(private val loadAccountUseCase: LoadAccountUseCase) :
    BaseViewModel<Account, MobileAccountIntent, Nothing>() {
    private var loading: Job? = null
    init { refresh() }
    override fun createInitialState(): Account = Account.Loading
    override fun handleIntent(intent: MobileAccountIntent) { refresh() }
    private fun refresh() {
        if (loading?.isActive == true) return
        loading = viewModelScope.launch {
            val result = loadAccountUseCase()
            updateState { result }
        }
    }
}
