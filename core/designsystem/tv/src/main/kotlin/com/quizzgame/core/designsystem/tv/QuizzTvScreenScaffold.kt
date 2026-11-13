package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme

/** TV не использует mobile TopAppBar/FAB. Панель экрана и уведомления задаются локальными slots. */
@Composable
fun QuizzTvScreenScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(
        modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(contentWindowInsets),
    ) {
        Column(Modifier.fillMaxSize()) {
            topBar()
            Box(Modifier.weight(1f)) { content(PaddingValues(0.dp)) }
        }
        Box(Modifier.align(Alignment.BottomCenter)) { snackbarHost() }
    }
}
