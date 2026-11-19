package com.quizzgame.feature.feed.tv.ui.rail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.shared.feed.domain.entity.section.QuizRail

@Composable
internal fun TvQuizRailHeader(rail: QuizRail, action: (@Composable () -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = QuizzTvLayout.screenInset.dp)
            .heightIn(min = QuizzTvLayout.railHeaderHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(QuizzTvLayout.itemGap.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rail.title?.let {
                Text(it, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            }
            rail.subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        action?.invoke()
    }
}
