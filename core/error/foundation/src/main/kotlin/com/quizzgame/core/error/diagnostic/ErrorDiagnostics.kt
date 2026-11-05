package com.quizzgame.core.error.diagnostic

fun interface ErrorDiagnostics {
    fun record(diagnostic: ErrorDiagnostic)
}
