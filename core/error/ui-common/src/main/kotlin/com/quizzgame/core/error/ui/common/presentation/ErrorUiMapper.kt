package com.quizzgame.core.error.ui.common.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.common.R

class ErrorUiMapper(private val text: (Int) -> String) {
    fun map(spec: ErrorPresentationSpec): ErrorUiModel {
        val copy = errorCopyResources(spec.title)
        return ErrorUiModel(
            title = text(copy.resource),
            message = spec.message?.let { text(errorCopyResources(it).resource) },
            icon = copy.icon,
            actions = spec.actions.map { ErrorUiAction(it, text(errorActionResource(it))) },
        )
    }
}

@Composable
fun errorUiModel(spec: ErrorPresentationSpec): ErrorUiModel {
    val configuration = LocalConfiguration.current
    val resources = LocalContext.current.createConfigurationContext(configuration).resources
    return ErrorUiMapper(resources::getString).map(spec)
}

fun requireErrorSurface(spec: ErrorPresentationSpec, expected: ErrorSurface) {
    require(spec.surface == expected) { "Error UI component requires $expected surface" }
}

internal fun errorActionResource(action: ErrorActionKind): Int = when (action) {
    ErrorActionKind.RETRY -> R.string.quizz_error_action_retry
    ErrorActionKind.CONFIRM_RETRY -> R.string.quizz_error_action_confirm_retry
    ErrorActionKind.GO_BACK -> R.string.quizz_error_action_go_back
    ErrorActionKind.SIGN_IN -> R.string.quizz_error_action_sign_in
    ErrorActionKind.OPEN_SETTINGS -> R.string.quizz_error_action_open_settings
    ErrorActionKind.DISMISS -> R.string.quizz_error_action_dismiss
    ErrorActionKind.EXIT -> R.string.quizz_error_action_exit
}
