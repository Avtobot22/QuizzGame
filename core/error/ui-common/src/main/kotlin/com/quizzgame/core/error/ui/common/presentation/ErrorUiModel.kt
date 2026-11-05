package com.quizzgame.core.error.ui.common.presentation

import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.ui.status.QuizzStatusIcon

data class ErrorUiModel(
    val title: String,
    val message: String?,
    val icon: QuizzStatusIcon,
    val actions: List<ErrorUiAction>,
)

data class ErrorUiAction(val kind: ErrorActionKind, val label: String)
internal data class ErrorCopyResources(val resource: Int, val icon: QuizzStatusIcon)
