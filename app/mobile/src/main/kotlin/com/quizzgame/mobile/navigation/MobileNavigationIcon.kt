package com.quizzgame.mobile.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.media.QuizzMobileMedia
import com.quizzgame.core.media.MediaSpec
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun MobileNavigationIcon(item: MobileTopLevelItem, size: Dp = 24.dp) {
    BadgedBox(badge = { item.badge?.let { Badge { Text(it.text) } } }) {
        val icon = item.media as? MediaSpec.Icon
        val resource = when (icon?.name) {
            "navigation.play" -> UiR.drawable.ic_quizz_room
            "navigation.explore" -> UiR.drawable.ic_quizz_quiz
            "navigation.library" -> UiR.drawable.ic_quizz_library
            "navigation.profile" -> UiR.drawable.ic_quizz_profile
            else -> item.icon
        }
        QuizzMobileMedia(icon ?: MediaSpec.Icon("local"), Modifier.size(size), localResource = resource)
    }
}
