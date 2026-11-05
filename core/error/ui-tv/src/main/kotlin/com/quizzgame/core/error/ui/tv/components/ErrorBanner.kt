package com.quizzgame.core.error.ui.tv.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.quizzgame.core.designsystem.tv.QuizzTvNoticeBanner
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.error.ui.common.presentation.requireErrorSurface
import com.quizzgame.core.ui.status.QuizzNoticeTone

@Composable
fun QuizzTvErrorBanner(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.BANNER)
    val model = errorUiModel(spec)
    QuizzTvNoticeBanner(modifier, QuizzNoticeTone.ERROR, model.icon, { ErrorActions(model, enabled, onAction) }) {
        ErrorCopy(model)
    }
}

@Preview(name = "Banner", locale = "ru", showBackground = true, widthDp = 960)
@Composable
private fun BannerPreview() {
    QuizzTvTheme {
        QuizzTvErrorBanner(
            spec = ErrorPresentationSpec(
                surface = ErrorSurface.BANNER,
                title = ErrorCopyKey.CONNECTION_PROBLEM,
                actions = listOf(ErrorActionKind.RETRY, ErrorActionKind.DISMISS),
            ),
            onAction = {},
        )
    }
}
