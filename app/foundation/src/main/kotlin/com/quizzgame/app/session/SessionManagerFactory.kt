package com.quizzgame.app.session

import com.quizzgame.core.session.lifecycle.SessionManager

fun interface SessionManagerFactory {
    suspend fun open(): SessionManager
}
