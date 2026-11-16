package com.quizzgame.core.designsystem.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quizzgame.core.ui.QuizzBrandLoading

@Composable
fun QuizzLoadingScreen(label: String, modifier: Modifier = Modifier) {
    QuizzBrandLoading(
        label = label,
        primary = MaterialTheme.colorScheme.primary,
        accent = MaterialTheme.colorScheme.tertiary,
        modifier = modifier,
    )
}
