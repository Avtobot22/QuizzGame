package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.tokens.QuizzTvLayout

/** Центрирует короткий экран и позволяет прокручивать содержимое, когда оно выше окна. */
@Composable
fun QuizzTvCenteredContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = Dp.Infinity,
    spacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val viewport = maxHeight
        Column(
            Modifier.widthIn(max = maxWidth).fillMaxWidth().verticalScroll(rememberScrollState())
                .heightIn(min = viewport)
                .padding(horizontal = QuizzTvLayout.screenInset.dp, vertical = QuizzTvLayout.verticalInset.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
            content = content,
        )
    }
}
