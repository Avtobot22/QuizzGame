package com.quizzgame.core.error.ui.common

import com.quizzgame.core.error.ErrorActionKind
import com.quizzgame.core.ui.status.QuizzStatusIcon

/** Данные для платформенных UI-адаптеров. Не являются моделью операции или серверного ответа. */
data class ErrorUiModel(
    val title: String,
    val message: String?,
    val icon: QuizzStatusIcon,
    val actions: List<ErrorUiAction>,
)

/** Визуальное действие адаптера с локальным текстом. */
data class ErrorUiAction(val kind: ErrorActionKind, val label: String)
internal data class ErrorCopyResources(val resource: Int, val icon: QuizzStatusIcon)
