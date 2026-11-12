package com.quizzgame.core.session.lifecycle

fun interface SessionStateReader {

    suspend fun current(): SessionState
}
