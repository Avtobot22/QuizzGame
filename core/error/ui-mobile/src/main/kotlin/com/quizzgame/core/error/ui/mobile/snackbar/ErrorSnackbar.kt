package com.quizzgame.core.error.ui.mobile.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.common.presentation.ErrorUiMapper
import com.quizzgame.core.error.ui.common.presentation.requireErrorSurface

suspend fun SnackbarHostState.showErrorSnackbar(
    spec: ErrorPresentationSpec,
    text: (Int) -> String,
): ErrorActionKind? {
    requireErrorSurface(spec, ErrorSurface.TRANSIENT)
    val model = ErrorUiMapper(text).map(spec)
    val actions = model.actions.filter { it.kind != ErrorActionKind.DISMISS }
    require(actions.size <= 1) { "A snackbar supports one primary action; use persistent error UI for more actions" }
    val action = actions.singleOrNull()
    val result = showSnackbar(
        message = listOfNotNull(model.title, model.message).joinToString("\n"),
        actionLabel = action?.label,
        withDismissAction = action != null || ErrorActionKind.DISMISS in spec.actions,
    )
    return if (result == SnackbarResult.ActionPerformed) action?.kind else null
}
