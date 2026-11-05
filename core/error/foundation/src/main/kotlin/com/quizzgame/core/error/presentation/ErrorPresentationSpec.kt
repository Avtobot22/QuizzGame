package com.quizzgame.core.error.presentation

data class ErrorPresentationSpec(
    val surface: ErrorSurface,
    val title: ErrorCopyKey,
    val message: ErrorCopyKey? = null,
    val actions: List<ErrorActionKind> = emptyList(),
) {
    init {
        require(surface != ErrorSurface.SILENT || actions.isEmpty()) { "Silent errors cannot offer actions" }
        require(actions.distinct().size == actions.size) { "Error actions must be unique" }
        require(ErrorActionKind.RETRY !in actions || ErrorActionKind.CONFIRM_RETRY !in actions) {
            "Retry must have a single confirmation contract"
        }
        require(ErrorActionKind.SIGN_IN !in actions || actions.none { it.isRetry() }) {
            "Sign-in and retry require different recovery decisions"
        }
    }
}

private fun ErrorActionKind.isRetry(): Boolean =
    this == ErrorActionKind.RETRY || this == ErrorActionKind.CONFIRM_RETRY
