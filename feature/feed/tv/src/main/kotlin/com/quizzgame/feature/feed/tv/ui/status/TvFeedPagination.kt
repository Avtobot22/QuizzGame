package com.quizzgame.feature.feed.tv.ui.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.tv.QuizzTvSkeleton
import com.quizzgame.core.designsystem.tv.QuizzTvSkeletonRail
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.feed.tv.R
import com.quizzgame.feature.feed.tv.ui.tags.TvFeedTags

@Composable
internal fun TvFeedAppendingSections() {
    QuizzTvSkeleton(
        stringResource(R.string.feed_loading_sections),
        Modifier.testTag(TvFeedTags.APPENDING_SECTIONS),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(QuizzTvLayout.sectionGap.dp)) {
            repeat(2) { QuizzTvSkeletonRail() }
        }
    }
}
