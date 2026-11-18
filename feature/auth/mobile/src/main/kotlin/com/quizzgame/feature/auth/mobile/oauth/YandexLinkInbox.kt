package com.quizzgame.feature.auth.mobile.oauth

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@ActivityRetainedScoped
class YandexLinkInbox @Inject constructor() {
    private val mutable = MutableStateFlow<String?>(null)
    val callback = mutable.asStateFlow()

    fun accept(attemptId: String) { mutable.value = attemptId }

    internal fun take(attemptId: String): Boolean = mutable.compareAndSet(attemptId, null)
}
