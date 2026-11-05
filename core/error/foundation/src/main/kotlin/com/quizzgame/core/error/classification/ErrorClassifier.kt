package com.quizzgame.core.error.classification

import com.quizzgame.core.error.failure.AppFailure
import com.quizzgame.core.network.result.NetworkFailure
import com.quizzgame.core.session.SessionState

interface ErrorClassifier {

    fun fromNetwork(failure: NetworkFailure): AppFailure

    fun fromSession(state: SessionState.Unavailable): AppFailure
}
