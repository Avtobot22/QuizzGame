package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.ui.QuizzSkeleton
import com.quizzgame.core.ui.quizzSkeleton

/** Показывает ожидание рядом с действием и объявляет доступную подпись. */
@Composable
fun QuizzTvProgressStrip(label: String, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    QuizzSkeleton(label, primary.copy(alpha = 0.25f), primary.copy(alpha = 0.75f), modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(4.dp).quizzSkeleton())
    }
}
