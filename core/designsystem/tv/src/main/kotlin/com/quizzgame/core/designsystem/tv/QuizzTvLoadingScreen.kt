package com.quizzgame.core.designsystem.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.ui.QuizzBrandLoading
import com.quizzgame.core.ui.QuizzBrandLoadingSize

@Composable
fun QuizzTvLoadingScreen(label: String, modifier: Modifier = Modifier) {
    QuizzBrandLoading(
        label = label,
        primary = MaterialTheme.colorScheme.primary,
        accent = MaterialTheme.colorScheme.tertiary,
        modifier = modifier,
        size = QuizzBrandLoadingSize.EXPANDED,
    )
}
