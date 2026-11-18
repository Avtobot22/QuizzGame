package com.quizzgame.feature.auth.tv.presentation

import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import com.quizzgame.core.presentation.BaseViewModel
import com.quizzgame.shared.auth.domain.AuthUseCase
import com.quizzgame.shared.auth.domain.OtpFlow
import com.quizzgame.shared.auth.domain.entity.AuthSurface
import com.quizzgame.shared.auth.domain.entity.OtpIntent
import com.quizzgame.shared.auth.domain.entity.OtpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TvSignInViewModel @Inject constructor(auth: AuthUseCase) :
    BaseViewModel<OtpState, OtpIntent, Nothing>() {
    private val otp = OtpFlow(auth, AuthSurface.TV, SystemClock::elapsedRealtime)
    private var operation: Job? = null

    init { handleIntent(OtpIntent.Load) }

    override fun createInitialState() = OtpState()

    override fun handleIntent(intent: OtpIntent) {
        if (operation?.isActive == true) {
            if (intent != OtpIntent.Back) return
            operation?.cancel()
        }
        operation = viewModelScope.launch {
            otp.handle(intent, state.value) { next -> updateState { next } }
        }
    }
}
