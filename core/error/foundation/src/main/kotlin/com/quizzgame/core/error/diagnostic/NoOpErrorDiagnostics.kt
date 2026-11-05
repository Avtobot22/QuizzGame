package com.quizzgame.core.error.diagnostic

data object NoOpErrorDiagnostics : ErrorDiagnostics {
    override fun record(diagnostic: ErrorDiagnostic) = Unit
}
