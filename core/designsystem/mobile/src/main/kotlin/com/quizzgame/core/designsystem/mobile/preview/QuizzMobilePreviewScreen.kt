package com.quizzgame.core.designsystem.mobile.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme

/** Экран превью со штатной темой, фоном и контейнером на весь доступный размер. */
@Composable
fun QuizzMobilePreviewScreen(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    QuizzMobileTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), content = content)
        }
    }
}
