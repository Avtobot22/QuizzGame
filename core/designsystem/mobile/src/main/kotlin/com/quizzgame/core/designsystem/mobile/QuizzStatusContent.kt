package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.quizzgame.core.ui.QuizzConnectionIllustration
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.core.ui.QuizzSceneIllustration
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusDimensions
import com.quizzgame.core.ui.status.QuizzStatusIcon
import com.quizzgame.core.ui.tokens.QuizzDimensions

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
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val accent = when (tone) {
        QuizzNoticeTone.NEUTRAL -> colors.onSurfaceVariant
        QuizzNoticeTone.INFORMATION -> colors.primary
        QuizzNoticeTone.ERROR -> colors.error
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = lerp(colors.surfaceContainerLow, accent, 0.045f),
        contentColor = colors.onSurface,
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = 0.28f),
        ),
    ) {
        Row(
            Modifier.heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 10.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(
                    Modifier.size(30.dp).background(accent.copy(alpha = 0.11f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(icon.resource), null, Modifier.size(18.dp), tint = accent)
                }
            }
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                content()
                actions()
            }
            trailing?.invoke()
        }
    }
}

/** Действие внутри уведомления, выровненное по тексту. */
@Composable
fun QuizzNoticeAction(label: String, icon: QuizzStatusIcon, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(
        onClick,
        Modifier.heightIn(min = QuizzDimensions.mobileMinTouchDp.dp),
        enabled = enabled,
        contentPadding = PaddingValues(end = 8.dp),
    ) {
        QuizzIconLabel(icon.resource, iconSize = 16.dp, tint = LocalContentColor.current) { Text(label) }
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
