package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.QuizzConnectionIllustration
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusDimensions
import com.quizzgame.core.ui.status.QuizzStatusIcon

@Composable
fun QuizzStatusContent(
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
    icon: QuizzStatusIcon? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
        Column(
            modifier.widthIn(max = QuizzStatusDimensions.mobileWidth.dp)
                .padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (icon == QuizzStatusIcon.ROOM) {
                QuizzConnectionIllustration(colors.primary, colors.tertiary, Modifier.size(180.dp))
            } else if (icon != null) {
                QuizzSceneIllustration(colors.primary, colors.tertiary, Modifier.size(160.dp)) {
                    QuizzStatusSymbol(
                        icon,
                        Modifier.size(48.dp),
                        tone,
                    )
                }
            }
            content()
            Box(Modifier.padding(4.dp), contentAlignment = Alignment.Center) { actions() }
        }
    }
}

@Composable
fun QuizzNoticeBanner(
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
        color = colors.surfaceContainer,
        contentColor = colors.onSurface,
        border = BorderStroke(
            1.dp,
            (if (tone == QuizzNoticeTone.ERROR) colors.error else colors.primary).copy(alpha = 0.3f),
        ),
    ) {
        Row(
            Modifier.padding(20.dp).semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (icon != null) QuizzStatusSymbol(icon, Modifier.size(28.dp), tone)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                content()
                actions()
            }
        }
    }
}

@Composable
fun QuizzInlineMessage(
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
    icon: QuizzStatusIcon? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (icon != null) QuizzStatusSymbol(icon, Modifier.size(24.dp), tone)
        content()
        actions()
    }
}

@Composable
fun QuizzStatusSymbol(
    icon: QuizzStatusIcon,
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
) {
    Icon(
        painterResource(icon.resource),
        contentDescription = null,
        modifier = modifier.size(QuizzStatusDimensions.mobileIcon.dp),
        tint = if (tone == QuizzNoticeTone.ERROR) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
    )
}
