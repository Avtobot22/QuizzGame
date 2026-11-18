package com.quizzgame.mobile.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.quizzgame.core.designsystem.mobile.media.readableColor
import com.quizzgame.core.designsystem.mobile.media.themeColor
import com.quizzgame.core.ui.tokens.QuizzDimensions
import com.quizzgame.feature.dashboard.mobile.domain.entity.NavigationAppearance
import com.quizzgame.mobile.ui.tags.MobileChromeTags

@Composable
internal fun MobileNavigationChrome(
    useRail: Boolean,
    active: NavKey,
    items: List<MobileTopLevelItem>,
    appearance: NavigationAppearance,
    select: (NavKey) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val background = appearance.backgroundColor.themeColor(scheme.surfaceContainer).let {
        when (appearance.background) {
            "transparent" -> it.copy(alpha = 0f)
            "translucent" -> it.copy(alpha = 0.9f)
            else -> it
        }
    }
    val selected = appearance.selectedColor.readableColor(scheme.primary, scheme.surfaceContainer)
    val unselected = appearance.unselectedColor.readableColor(scheme.onSurfaceVariant, scheme.surfaceContainer)
    if (useRail) {
        NavigationRail(Modifier.testTag(MobileChromeTags.RAIL), containerColor = background) {
            items.forEach { item ->
                NavigationRailItem(
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = selected,
                        selectedTextColor = selected,
                        unselectedIconColor = unselected,
                        unselectedTextColor = unselected,
                    ),
                    selected = active == item.route,
                    onClick = { select(item.route) },
                    icon = { MobileNavigationIcon(item) },
                    label = { MobileNavigationLabel(item) },
                    modifier = Modifier.testTag(MobileChromeTags.item(item.route)),
                )
            }
        }
    } else {
        NavigationBar(Modifier.testTag(MobileChromeTags.BAR), containerColor = background) {
            items.forEach { item ->
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = selected,
                        selectedTextColor = selected,
                        unselectedIconColor = unselected,
                        unselectedTextColor = unselected,
                    ),
                    selected = active == item.route,
                    onClick = { select(item.route) },
                    icon = { MobileNavigationIcon(item, QuizzDimensions.mobileNavigationIconDp.dp) },
                    label = { MobileNavigationLabel(item) },
                    modifier = Modifier.testTag(MobileChromeTags.item(item.route)),
                )
            }
        }
    }
}

@Composable
private fun MobileNavigationLabel(item: MobileTopLevelItem) {
    Text(item.title ?: stringResource(item.label), maxLines = 1, overflow = TextOverflow.Ellipsis)
}
