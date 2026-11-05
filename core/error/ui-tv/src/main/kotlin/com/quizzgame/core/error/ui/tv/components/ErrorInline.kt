package com.quizzgame.core.error.ui.tv.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.quizzgame.core.designsystem.tv.QuizzTvInlineMessage
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.error.ui.common.presentation.requireErrorSurface
import com.quizzgame.core.ui.status.QuizzNoticeTone

@Composable
fun QuizzTvErrorInline(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.INLINE)
    val model = errorUiModel(spec)
    QuizzTvInlineMessage(modifier, QuizzNoticeTone.ERROR, model.icon, { ErrorActions(model, enabled, onAction) }) {
        ErrorCopy(model)
    }
}

@Preview(name = "Inline", locale = "ru", showBackground = true, widthDp = 960)
@Composable
private fun InlinePreview() {
    QuizzTvTheme {
        QuizzTvErrorInline(
            spec = ErrorPresentationSpec(
                surface = ErrorSurface.INLINE,
                title = ErrorCopyKey.REQUEST_TIMED_OUT,
                actions = listOf(ErrorActionKind.CONFIRM_RETRY),
            ),
            onAction = {},
        )
    }
}
