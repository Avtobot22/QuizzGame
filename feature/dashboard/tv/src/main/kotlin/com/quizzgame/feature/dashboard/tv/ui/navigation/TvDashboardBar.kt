package com.quizzgame.feature.dashboard.tv.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.designsystem.tv.QuizzTvButtonTone
import com.quizzgame.core.designsystem.tv.QuizzTvNavigationBar
import com.quizzgame.core.designsystem.tv.media.QuizzTvBadge
import com.quizzgame.core.designsystem.tv.media.QuizzTvMedia
import com.quizzgame.core.designsystem.tv.media.themeColor
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.dashboard.tv.R
import com.quizzgame.feature.dashboard.tv.domain.entity.Dashboard
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTab
import com.quizzgame.feature.dashboard.tv.domain.entity.DashboardTabId
import com.quizzgame.feature.dashboard.tv.ui.tags.TvDashboardTags
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvDashboardBar(
    dashboard: Dashboard,
    selected: DashboardTabId,
    focus: FocusRequester,
    requestFocus: Boolean,
    onLogout: () -> Unit,
    onSelect: (DashboardTabId) -> Unit,
    onBarFocus: () -> Unit,
) {
    val appearance = dashboard.appearance
    val background = appearance.backgroundColor.themeColor(MaterialTheme.colorScheme.background)
    QuizzTvNavigationBar(
        items = dashboard.tabs.map { it.id },
        selected = selected,
        onSelect = onSelect,
        enabled = { id -> dashboard.tabs.any { it.id == id && it.enabled } },
        modifier = Modifier.background(
            when (appearance.background) {
                "transparent" -> Color.Transparent
                "translucent" -> background.copy(alpha = 0.85f)
                else -> background
            },
        ).padding(horizontal = QuizzTvLayout.screenInset.dp, vertical = QuizzTvLayout.verticalInset.dp)
            .testTag(TvDashboardTags.NAVIGATION),
        itemModifier = { id ->
            Modifier.testTag(TvDashboardTags.tab(id)).onFocusChanged { if (it.isFocused) onBarFocus() }
                .then(if (id == selected) Modifier.focusRequester(focus) else Modifier)
        },
        label = { id -> TvDashboardTabLabel(dashboard.tabs.first { it.id == id }) },
        trailingContent = {
            QuizzTvButton(
                onLogout,
                Modifier.width(QuizzTvLayout.navigationActionWidth.dp).testTag(TvDashboardTags.SIGN_OUT),
                style = QuizzTvButtonStyle.QUIET,
                tone = QuizzTvButtonTone.DANGER,
            ) {
                Icon(painterResource(UiR.drawable.ic_quizz_logout), null, Modifier.size(18.dp))
                QuizzTvActionLabel(stringResource(R.string.dashboard_logout), Modifier.padding(start = 8.dp))
            }
        },
    )
    TvEntryFocus(focus, requestFocus)
}

@Composable
private fun TvDashboardTabLabel(tab: DashboardTab) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val resource = when (tab.id) {
            DashboardTabId.FEED -> UiR.drawable.ic_quizz_home
            DashboardTabId.LIBRARY -> UiR.drawable.ic_quizz_library
            DashboardTabId.SETTINGS -> UiR.drawable.ic_quizz_settings
        }
        if (tab.icon is MediaSpec.Remote || tab.icon is MediaSpec.Embedded) {
            QuizzTvMedia(tab.icon, Modifier.size(18.dp))
        } else {
            Icon(painterResource(resource), null, Modifier.size(18.dp))
        }
        QuizzTvActionLabel(tab.title, Modifier.weight(1f, fill = false))
        tab.badge?.let { QuizzTvBadge(it.text, it.live, foreground = it.foreground, background = it.background) }
    }
}
