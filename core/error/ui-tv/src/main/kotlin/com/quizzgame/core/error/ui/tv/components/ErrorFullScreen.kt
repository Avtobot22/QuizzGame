package com.quizzgame.core.error.ui.tv.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.tv.QuizzTvStatusContent
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.error.presentation.ErrorActionKind
import com.quizzgame.core.error.presentation.ErrorCopyKey
import com.quizzgame.core.error.presentation.ErrorPresentationSpec
import com.quizzgame.core.error.presentation.ErrorSurface
import com.quizzgame.core.error.ui.common.presentation.errorUiModel
import com.quizzgame.core.error.ui.common.presentation.requireErrorSurface
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusDimensions
import com.quizzgame.core.ui.tokens.QuizzDimensions

@Composable
fun QuizzTvErrorFullScreen(
    spec: ErrorPresentationSpec,
    onAction: (ErrorActionKind) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    requestInitialFocus: Boolean = true,
) {
    requireErrorSurface(spec, ErrorSurface.BLOCKING)
    val model = errorUiModel(spec)
    val firstAction = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(
                horizontal = QuizzDimensions.tvSafeHorizontalDp.dp,
                vertical = QuizzDimensions.tvSafeVerticalDp.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = QuizzStatusDimensions.tvWidth.dp)
                .fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
            QuizzTvStatusContent(
                modifier = Modifier.fillMaxWidth(),
                tone = QuizzNoticeTone.ERROR,
                icon = model.icon,
                actions = { ErrorActions(model, enabled, onAction, firstAction) },
            ) { ErrorCopy(model, heading = true) }
        }
    }
    val canRequestFocus = enabled && requestInitialFocus && model.actions.isNotEmpty()
    LaunchedEffect(canRequestFocus) {
        if (canRequestFocus && !focused) {
            focused = firstAction.requestFocus()
        }
    }
}

@Preview(name = "FullScreen", locale = "ru", showBackground = true, widthDp = 960, heightDp = 540)
@Composable
private fun FullScreenPreview() {
    QuizzTvTheme {
        QuizzTvErrorFullScreen(
            spec = ErrorPresentationSpec(
                surface = ErrorSurface.BLOCKING,
                title = ErrorCopyKey.SERVER_UNAVAILABLE,
                actions = listOf(ErrorActionKind.RETRY),
            ),
            onAction = {},
            requestInitialFocus = false,
        )
    }
}
