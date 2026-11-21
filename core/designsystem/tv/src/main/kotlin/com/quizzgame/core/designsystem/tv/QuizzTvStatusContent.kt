package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
    val accent = when (tone) {
        QuizzNoticeTone.NEUTRAL -> colors.onSurfaceVariant
        QuizzNoticeTone.INFORMATION -> colors.primary
        QuizzNoticeTone.ERROR -> colors.error
    }
    Surface(
        modifier = modifier.fillMaxWidth().border(
            1.dp,
            accent.copy(alpha = 0.25f),
            MaterialTheme.shapes.large,
        ),
        shape = MaterialTheme.shapes.large,
        colors = SurfaceDefaults.colors(
            containerColor = lerp(colors.surface, accent, 0.055f),
            contentColor = colors.onSurface,
        ),
    ) {
        Row(
            Modifier.heightIn(min = 60.dp).padding(horizontal = 14.dp, vertical = 10.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(
                    Modifier.size(36.dp).background(accent.copy(alpha = 0.13f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(icon.resource), null, Modifier.size(21.dp), tint = accent)
                }
            }
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
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
