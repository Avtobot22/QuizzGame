package com.quizzgame.core.error.ui.mobile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.designsystem.mobile.QuizzStatusContent
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.error.ui.common.presentation.requireErrorSurface
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusDimensions

@Composable
fun QuizzMobileErrorFullScreen(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.BLOCKING)
    val model = errorUiModel(spec)
    Box(
        modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = QuizzStatusDimensions.mobileWidth.dp)
                .fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
            QuizzStatusContent(
                modifier = Modifier.fillMaxWidth(),
                tone = QuizzNoticeTone.ERROR,
                icon = model.icon,
                actions = { ErrorActions(model, enabled, onAction) },
            ) { ErrorCopy(model, heading = true) }
        }
    }
}

@Preview(name = "FullScreen", locale = "ru", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FullScreenPreview() {
    QuizzMobileTheme {
        QuizzMobileErrorFullScreen(
            spec = ErrorPresentationSpec(
                surface = ErrorSurface.BLOCKING,
                title = ErrorCopyKey.SERVER_UNAVAILABLE,
                actions = listOf(ErrorActionKind.RETRY),
            ),
            onAction = {},
        )
    }
}
