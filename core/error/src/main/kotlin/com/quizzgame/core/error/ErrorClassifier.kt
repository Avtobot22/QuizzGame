package com.quizzgame.core.error

import com.quizzgame.core.network.NetworkFailure
import com.quizzgame.core.session.SessionState

interface ErrorClassifier {

    fun fromNetwork(failure: NetworkFailure): AppFailure

    fun fromSession(state: SessionState.Unavailable): AppFailure
}
