package com.quizzgame.app.startup

import com.quizzgame.app.graph.AppGraph

sealed interface GraphAccessResult {
    data class Ready(val graph: AppGraph) : GraphAccessResult
    data class Unavailable(val failure: AppStartupState.Failed) : GraphAccessResult
    data object Closed : GraphAccessResult
}
