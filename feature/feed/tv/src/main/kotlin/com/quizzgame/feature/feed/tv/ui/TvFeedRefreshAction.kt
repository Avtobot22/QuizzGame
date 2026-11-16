package com.quizzgame.feature.feed.tv.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.feature.feed.tv.R
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvFeedRefreshAction(refreshing: Boolean, onRefresh: () -> Unit) {
    QuizzTvButton(
        onRefresh,
        Modifier.testTag(TvFeedTags.REFRESH),
        enabled = !refreshing,
        style = QuizzTvButtonStyle.SECONDARY,
    ) {
        Icon(painterResource(UiR.drawable.ic_quizz_refresh), null, Modifier.size(18.dp))
        QuizzTvActionLabel(
            stringResource(if (refreshing) R.string.feed_refreshing else R.string.feed_refresh),
            Modifier.padding(start = 8.dp),
        )
    }
}
