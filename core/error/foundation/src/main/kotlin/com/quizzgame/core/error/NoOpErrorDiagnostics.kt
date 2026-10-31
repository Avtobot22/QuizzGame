package com.quizzgame.core.error

/** Приёмник по умолчанию: не сохраняет конверт, не выполняет I/O и не генерирует идентификаторы. */
data object NoOpErrorDiagnostics : ErrorDiagnostics {
    override fun record(diagnostic: ErrorDiagnostic) = Unit
}
