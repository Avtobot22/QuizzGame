package com.quizzgame.feature.quizentry.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileLayout
import com.quizzgame.core.designsystem.mobile.QuizzMobileSkeleton
import com.quizzgame.core.designsystem.mobile.QuizzSkeletonLine
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.core.ui.tokens.QuizzDimensions
import com.quizzgame.feature.quizentry.mobile.R
import com.quizzgame.feature.quizentry.mobile.ui.tags.MobileQuizEntryTags

@Composable
internal fun MobileQuizEntrySkeleton(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        QuizzMobileSkeleton(
            stringResource(R.string.entry_loading),
            Modifier.widthIn(max = QuizzMobileLayout.contentWidth).fillMaxWidth().testTag(MobileQuizEntryTags.LOADING),
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(QuizzMobileLayout.pageGutter),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(QuizzMobileLayout.COVER_ASPECT_RATIO)
                        .quizzSkeleton(MaterialTheme.shapes.extraLarge),
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuizzSkeletonLine(Modifier.fillMaxWidth(0.25f))
                    QuizzSkeletonLine(Modifier.fillMaxWidth(0.95f).height(28.dp))
                    QuizzSkeletonLine(Modifier.fillMaxWidth(0.7f).height(28.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f).height(40.dp).quizzSkeleton(MaterialTheme.shapes.medium))
                        Box(Modifier.weight(1f).height(40.dp).quizzSkeleton(MaterialTheme.shapes.medium))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuizzSkeletonLine(Modifier.fillMaxWidth(0.65f))
                    QuizzSkeletonLine(Modifier.fillMaxWidth())
                    QuizzSkeletonLine(Modifier.fillMaxWidth(0.9f))
                }
                Box(
                    Modifier.fillMaxWidth().height(QuizzDimensions.mobileMinTouchDp.dp)
                        .quizzSkeleton(MaterialTheme.shapes.large),
                )
            }
        }
    }
}
