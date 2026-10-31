package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.R
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusDimensions
import com.quizzgame.core.ui.status.QuizzStatusIcon

/**
 * Визуальное состояние с произвольными content/actions. Подходит для пустого, успешного,
 * информационного или ошибочного состояния. Полную область, прокрутку, insets и начальный фокус
 * задаёт внешний shell; компонент не меняет навигацию и не выполняет действия сам.
 */
@Composable
fun QuizzStatusContent(
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
    icon: QuizzStatusIcon? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    StatusPanel(modifier.widthIn(max = QuizzStatusDimensions.mobileWidth.dp), tone) {
        MessageBody(Modifier.padding(QuizzStatusDimensions.padding.dp), icon, actions, content)
    }
}

/** Встроенное сообщение рядом с сохранённым содержимым. Не забирает фокус и не закрывается само. */
@Composable
fun QuizzNoticeBanner(
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.INFORMATION,
    icon: QuizzStatusIcon? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    StatusPanel(modifier.fillMaxWidth(), tone) {
        MessageBody(Modifier.padding(QuizzStatusDimensions.compactPadding.dp), icon, actions, content)
    }
}

/** Локальное сообщение без панели. Размещение возле нужного поля определяет владелец формы. */
@Composable
fun QuizzInlineMessage(
    modifier: Modifier = Modifier,
    tone: QuizzNoticeTone = QuizzNoticeTone.NEUTRAL,
    icon: QuizzStatusIcon? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    CompositionLocalProvider(
        LocalContentColor provides if (tone == QuizzNoticeTone.ERROR) colors.error else colors.onSurface,
    ) {
        MessageBody(modifier, icon, actions, content)
    }
}

@Composable
private fun StatusPanel(modifier: Modifier, tone: QuizzNoticeTone, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val container = when (tone) {
        QuizzNoticeTone.NEUTRAL -> colors.surfaceVariant
        QuizzNoticeTone.INFORMATION -> colors.secondaryContainer
        QuizzNoticeTone.ERROR -> colors.errorContainer
    }
    val foreground = when (tone) {
        QuizzNoticeTone.NEUTRAL -> colors.onSurfaceVariant
        QuizzNoticeTone.INFORMATION -> colors.onSecondaryContainer
        QuizzNoticeTone.ERROR -> colors.onErrorContainer
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = foreground,
    ) { content() }
}

@Composable
private fun MessageBody(
    modifier: Modifier,
    icon: QuizzStatusIcon?,
    actions: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(QuizzStatusDimensions.gap.dp),
    ) {
        if (icon != null) QuizzStatusSymbol(icon)
        content()
        actions()
    }
}

/** Декоративная семантическая пиктограмма; текст сообщения содержит доступное описание. */
@Composable
fun QuizzStatusSymbol(icon: QuizzStatusIcon, modifier: Modifier = Modifier) {
    val resource = when (icon) {
        QuizzStatusIcon.NETWORK -> R.drawable.ic_quizz_status_network
        QuizzStatusIcon.SERVER -> R.drawable.ic_quizz_status_server
        QuizzStatusIcon.AUTH -> R.drawable.ic_quizz_status_auth
        QuizzStatusIcon.GENERIC -> R.drawable.ic_quizz_status_generic
    }
    Icon(painterResource(resource), contentDescription = null, modifier.size(QuizzStatusDimensions.mobileIcon.dp))
}
