package com.quizzgame.feature.feed.tv.ui.rail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.tv.QuizzTvSkeleton
import com.quizzgame.core.designsystem.tv.QuizzTvSkeletonCard
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.feed.tv.R
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags

@Composable
internal fun TvRailAppending(sectionId: String) {
    QuizzTvSkeleton(
        stringResource(R.string.feed_loading_cards),
        Modifier.testTag(TvFeedTags.appendingRail(sectionId)),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(QuizzTvLayout.itemGap.dp)) {
            repeat(2) { QuizzTvSkeletonCard() }
        }
    }
}
