package com.quizzgame.feature.play.mobile.presentation

import com.quizzgame.core.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MobilePlayViewModel @Inject constructor() :
    BaseViewModel<MobilePlayState, MobilePlayIntent, Nothing>() {
    override fun createInitialState() = MobilePlayState.READY
    override fun handleIntent(intent: MobilePlayIntent) {
        updateState {
            when (intent) {
                MobilePlayIntent.CONNECT, MobilePlayIntent.SCAN -> MobilePlayState.UNAVAILABLE
                MobilePlayIntent.DISMISS -> MobilePlayState.READY
            }
        }
    }
}
