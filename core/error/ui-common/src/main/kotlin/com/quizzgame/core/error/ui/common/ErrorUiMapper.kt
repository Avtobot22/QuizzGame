package com.quizzgame.core.error.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.error.ErrorPresentationSpec
import com.quizzgame.core.error.ErrorSurface

/** Инфраструктурный маппер адаптеров; разрешает только локальные ключи, не меняя policy result. */
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

/** Актуальная локализация для платформенного адаптера, без глобального кэша строк. */
@Composable
fun errorUiModel(spec: ErrorPresentationSpec): ErrorUiModel {
    // Чтение configuration делает смену locale входом композиции; строки не кэшируются глобально.
    val configuration = LocalConfiguration.current
    val resources = LocalContext.current.createConfigurationContext(configuration).resources
    return ErrorUiMapper(resources::getString).map(spec)
}

/** Проверка интеграционного контракта специализированного адаптера. */
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
