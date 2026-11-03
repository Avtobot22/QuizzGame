package com.quizzgame.core.navigation

import android.os.Bundle
import java.util.UUID

object ProcessNavigationGuard {
    private const val PROCESS_KEY = "quizz.navigation.process"
    private val processId = UUID.randomUUID().toString()

    fun restore(savedState: Bundle?): Bundle? = savedState?.takeIf {
        mayRestoreNavigation(it.getString(PROCESS_KEY), processId)
    }

    fun save(outState: Bundle) { outState.putString(PROCESS_KEY, processId) }
}
