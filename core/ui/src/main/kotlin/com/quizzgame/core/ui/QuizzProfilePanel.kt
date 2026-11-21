package com.quizzgame.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

/** Общая карточка для данных профиля и плейсхолдеров той же формы. */
@Composable
fun QuizzProfilePanel(
    surface: Color,
    accent: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val gradient = Brush.linearGradient(listOf(lerp(surface, accent, 0.42f), surface))
    Column(
        modifier.fillMaxWidth().background(gradient, shape).border(1.dp, accent.copy(alpha = 0.35f), shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

/** Аватар и подписи профиля занимают одинаковые места в готовом состоянии и загрузке. */
@Composable
fun QuizzProfileIdentity(avatar: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        avatar()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp), content = content)
    }
}
