package com.quizzgame.core.designsystem.tv.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvStatusContent
import com.quizzgame.core.designsystem.tv.QuizzTvStatusCopy
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.tokens.QuizzTvLayout

@Composable
fun QuizzTvEmptyState(
    label: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    requestFocus: Boolean = true,
) {
    val focus = remember { FocusRequester() }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        QuizzTvStatusContent(
            Modifier.widthIn(max = QuizzTvLayout.statusWidth.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            icon = QuizzStatusIcon.CATALOG,
            actions = {
                QuizzTvButton(onRetry, Modifier.focusRequester(focus)) { QuizzTvActionLabel(retryLabel) }
            },
        ) {
            QuizzTvStatusCopy(label, description)
        }
    }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(focus, owner, requestFocus) {
        if (requestFocus) owner.lifecycle.withResumed { focus.requestFocus() }
    }
}
