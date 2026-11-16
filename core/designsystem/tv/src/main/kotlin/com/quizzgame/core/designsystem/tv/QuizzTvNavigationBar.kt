package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.SelectableSurfaceDefaults
import androidx.tv.material3.Surface
import com.quizzgame.core.ui.tokens.QuizzTvLayout

@Composable
fun <T> QuizzTvNavigationBar(
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: (T) -> Boolean = { true },
    itemModifier: (T) -> Modifier = { Modifier },
    leadingContent: @Composable () -> Unit = {},
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth().focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(QuizzTvLayout.itemGap.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent()
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { item ->
                NavigationTab(
                    selected = selected == item,
                    enabled = enabled(item),
                    onClick = { onSelect(item) },
                    modifier = Modifier.weight(1f).then(itemModifier(item)),
                ) { label(item) }
            }
        }
        trailingContent()
    }
}

@Composable
private fun NavigationTab(
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Surface(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = SelectableSurfaceDefaults.shape(shape),
        colors = SelectableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = colors.onSurfaceVariant,
            selectedContainerColor = colors.surfaceVariant.copy(alpha = 0.4f),
            selectedContentColor = colors.onSurface,
            focusedContainerColor = colors.surfaceVariant,
            focusedContentColor = colors.onSurface,
            focusedSelectedContainerColor = colors.surfaceVariant,
            focusedSelectedContentColor = colors.onSurface,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = colors.onSurface.copy(alpha = 0.38f),
        ),
        scale = SelectableSurfaceDefaults.scale(focusedScale = 1f, focusedSelectedScale = 1f),
        border = SelectableSurfaceDefaults.border(),
        glow = SelectableSurfaceDefaults.glow(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = QuizzTvLayout.buttonHeight.dp)
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) { content() }
            if (selected) {
                Box(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).width(24.dp).height(2.dp)
                        .background(colors.primary, MaterialTheme.shapes.small),
                )
            }
        }
    }
}
