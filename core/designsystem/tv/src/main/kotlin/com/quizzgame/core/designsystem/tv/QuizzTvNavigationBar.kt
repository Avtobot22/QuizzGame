package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Горизонтальное Dashboard-меню. Выбор раздела и фокус принадлежат платформенному host. */
@Composable
fun <T> QuizzTvNavigationBar(
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: (T) -> Boolean = { true },
    itemModifier: (T) -> Modifier = { Modifier },
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        ).focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items.forEach { item ->
            QuizzTvButton(
                onClick = { onSelect(item) },
                enabled = enabled(item),
                modifier = itemModifier(item).semantics { this.selected = selected == item },
            ) { label(item) }
        }
        trailingContent()
    }
}
