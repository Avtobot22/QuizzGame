package com.quizzgame.core.session.api

class RefreshEndpoint(val path: String) {
    init {
        require(PATH.matches(path)) { "Refresh endpoint must be a relative path of plain segments" }
    }

    companion object {

        private val PATH = Regex("[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*")
    }
}
