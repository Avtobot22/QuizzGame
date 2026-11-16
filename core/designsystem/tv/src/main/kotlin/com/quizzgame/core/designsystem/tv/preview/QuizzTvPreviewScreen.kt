package com.quizzgame.core.designsystem.tv.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.quizzgame.core.designsystem.tv.QuizzTvTheme

/** Экран превью со штатной темой, фоном и контейнером на весь доступный размер. */
@Composable
fun QuizzTvPreviewScreen(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    QuizzTvTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        ) {
            Box(Modifier.fillMaxSize(), content = content)
        }
    }
}
