package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/** Отметка сохранённого элемента. Доступное имя можно передать отдельно от видимой подписи. */
@Composable
fun QuizzMobileFavoriteMark(contentDescription: String?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(modifier.size(32.dp).background(colors.tertiaryContainer, CircleShape), contentAlignment = Alignment.Center) {
        Icon(
            painterResource(R.drawable.ic_quizz_favorite),
            contentDescription,
            Modifier.size(16.dp),
            tint = colors.onTertiaryContainer,
        )
    }
}
