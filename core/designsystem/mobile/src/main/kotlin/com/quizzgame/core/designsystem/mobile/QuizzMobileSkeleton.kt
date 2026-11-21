package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.QuizzSkeleton
import com.quizzgame.core.ui.quizzSkeleton

/** Одна анимация и одно доступное описание для всей группы плейсхолдеров. */
@Composable
fun QuizzMobileSkeleton(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    QuizzSkeleton(label, colors.surfaceContainerHigh, colors.surfaceContainerLowest, modifier, content)
}

@Composable
fun QuizzSkeletonLine(modifier: Modifier = Modifier) {
    Box(modifier.height(16.dp).quizzSkeleton(MaterialTheme.shapes.small))
}
