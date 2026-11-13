package com.quizzgame.shared.splash.data

import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.core.session.lifecycle.SessionStateReader
import com.quizzgame.shared.splash.domain.entity.CurrentUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CurrentUserStore @Inject constructor(private val session: SessionStateReader) {

    @Volatile
    private var record: Pair<Long, CurrentUser>? = null

    suspend fun replace(generation: Long, user: CurrentUser): Boolean {
        if (session.current() != SessionState.Authenticated(generation)) return false
        record = generation to user
        return true
    }

    suspend fun current(): CurrentUser? {
        val saved = record ?: return null
        if (session.current() == SessionState.Authenticated(saved.first)) return saved.second
        record = null
        return null
    }

    fun clear() {
        record = null
    }
}
