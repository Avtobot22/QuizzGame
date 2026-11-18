package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun QuizzAccountSkeleton(label: String, modifier: Modifier = Modifier) {
    QuizzMobileSkeleton(label, modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.fillMaxWidth().height(120.dp).quizzSkeleton(MaterialTheme.shapes.extraLarge))
            QuizzSkeletonLine(Modifier.fillMaxWidth(0.6f))
            QuizzSkeletonLine(Modifier.fillMaxWidth(0.85f))
            Box(
                Modifier.padding(top = 12.dp).fillMaxWidth().height(160.dp)
                    .quizzSkeleton(MaterialTheme.shapes.extraLarge),
            )
        }
    }
}
