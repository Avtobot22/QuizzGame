package com.quizzgame.core.error.operation

data class ErrorOperationContext(
    val operation: OperationKind,
    val hasUsableContent: Boolean,
    val retrySafety: RetrySafety,
)
