package com.quizzgame.feature.feed.mobile.ui.status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.core.designsystem.mobile.QuizzMobileSkeleton
import com.quizzgame.core.designsystem.mobile.QuizzSkeletonLine
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.feature.feed.mobile.R
import com.quizzgame.feature.feed.mobile.ui.card.RailCopyPadding
import com.quizzgame.feature.feed.mobile.ui.card.RailCopySpacing
import com.quizzgame.feature.feed.mobile.ui.card.railBadgeHeight
import com.quizzgame.feature.feed.mobile.ui.card.railCardHeight

@Composable
internal fun MobileFeedSkeleton(modifier: Modifier = Modifier) {
    QuizzMobileSkeleton(stringResource(R.string.feed_loading), modifier.fillMaxSize()) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            FeaturedSectionPlaceholder()
            FeedRailPlaceholders()
        }
    }
}

@Composable
private fun FeaturedSectionPlaceholder() {
    Column(verticalArrangement = Arrangement.spacedBy(QuizzMobileLayout.pageGutter)) {
        QuizzSkeletonLine(Modifier.fillMaxWidth(0.42f).height(24.dp))
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column {
                Box(Modifier.fillMaxWidth().aspectRatio(2f).quizzSkeleton(MaterialTheme.shapes.extraLarge))
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuizzSkeletonLine(Modifier.fillMaxWidth(0.55f).height(24.dp))
                    QuizzSkeletonLine(Modifier.fillMaxWidth(0.85f).height(32.dp))
                    QuizzSkeletonLine(Modifier.fillMaxWidth(0.65f).height(24.dp))
                }
            }
        }
    }
}

@Composable
internal fun MobileFeedAppendSkeleton(modifier: Modifier = Modifier) {
    QuizzMobileSkeleton(stringResource(R.string.feed_loading), modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) { FeedRailPlaceholders() }
    }
}

@Composable
private fun FeedRailPlaceholders() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        QuizzSkeletonLine(Modifier.fillMaxWidth(0.6f).height(24.dp))
        QuizzSkeletonLine(Modifier.fillMaxWidth(0.4f))
        Row(
            Modifier.horizontalScroll(rememberScrollState(), enabled = false),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(2) { FeedCardPlaceholder(Modifier.width(QuizzMobileLayout.railCardWidth)) }
        }
    }
}

@Composable
internal fun MobileRailSkeleton(modifier: Modifier = Modifier) {
    QuizzMobileSkeleton(stringResource(R.string.feed_loading), modifier) { FeedCardPlaceholder(Modifier) }
}

@Composable
private fun FeedCardPlaceholder(modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier.widthIn(max = QuizzMobileLayout.railCardWidth).fillMaxWidth().height(railCardHeight()),
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().aspectRatio(QuizzMobileLayout.COVER_ASPECT_RATIO)
                    .quizzSkeleton(MaterialTheme.shapes.large),
            )
            Column(
                Modifier.padding(RailCopyPadding),
                verticalArrangement = Arrangement.spacedBy(RailCopySpacing),
            ) {
                QuizzSkeletonLine(Modifier.width(62.dp).height(railBadgeHeight()))
                QuizzSkeletonLine(Modifier.fillMaxWidth(0.9f))
                QuizzSkeletonLine(Modifier.fillMaxWidth(0.62f))
            }
        }
    }
}
