package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.quizzgame.core.ui.QuizzConnectionIllustration
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusDimensions
import com.quizzgame.core.ui.status.QuizzStatusIcon

@Composable
fun QuizzTvStatusContent(
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
    icon: QuizzStatusIcon? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
        Column(
            modifier.widthIn(max = QuizzStatusDimensions.tvWidth.dp)
                .padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (icon == QuizzStatusIcon.ROOM) {
                QuizzConnectionIllustration(colors.primary, colors.tertiary, Modifier.size(180.dp))
            } else if (icon != null) {
                QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(144.dp)) {
                    QuizzTvStatusSymbol(
                        icon,
                        Modifier.size(44.dp),
                        tone,
                    )
                }
            }
            content()
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) { actions() }
        }
    }
}

@Composable
fun QuizzTvNoticeBanner(
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.INFORMATION,
    icon: QuizzStatusIcon? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = SurfaceDefaults.colors(
            containerColor = lerp(colors.surface, colors.surfaceVariant, 0.28f),
            contentColor = colors.onSurface,
        ),

    ) {
        Row(
            Modifier.padding(20.dp).semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (icon != null) QuizzTvStatusSymbol(icon, Modifier.size(28.dp), tone)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                content()
                actions()
            }
        }
    }
}

@Composable
fun QuizzTvInlineMessage(
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
    icon: QuizzStatusIcon? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (icon != null) QuizzTvStatusSymbol(icon, Modifier.size(24.dp), tone)
        content()
        actions()
    }
}

@Composable
fun QuizzTvStatusSymbol(
    icon: QuizzStatusIcon,
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
) {
    Icon(
        painterResource(icon.resource),
        contentDescription = null,
        modifier = modifier.size(QuizzStatusDimensions.tvIcon.dp),
        tint = if (tone == QuizzNoticeTone.ERROR) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
    )
}
