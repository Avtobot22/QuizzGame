package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.ui.QuizzSkeleton
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.core.ui.tokens.QuizzTvLayout

@Composable
fun QuizzTvSkeleton(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    QuizzSkeleton(
        label,
        lerp(colors.surface, colors.onSurface, 0.07f),
        lerp(colors.surface, colors.onSurface, 0.14f),
        modifier,
        content,
    )
}

@Composable
fun QuizzTvCatalogSkeleton(label: String, modifier: Modifier = Modifier, showNavigation: Boolean = false) {
    QuizzTvSkeleton(label, modifier.fillMaxSize()) {
        Column {
            if (showNavigation) NavigationSkeleton()
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(QuizzTvLayout.sectionGap.dp),
            ) {
                items(3, key = { "skeleton-section-$it" }) { QuizzTvSkeletonRail() }
            }
        }
    }
}

@Composable
private fun NavigationSkeleton() {
    Box(
        Modifier.padding(horizontal = QuizzTvLayout.screenInset.dp, vertical = QuizzTvLayout.verticalInset.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(QuizzTvLayout.buttonHeight.dp).quizzSkeleton(MaterialTheme.shapes.medium))
    }
}

@Composable
fun QuizzTvSkeletonRail(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(
            Modifier.padding(
                horizontal = QuizzTvLayout.screenInset.dp,
            ).heightIn(min = QuizzTvLayout.railHeaderHeight.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(Modifier.width(200.dp).height(28.dp).quizzSkeleton())
            Box(Modifier.width(140.dp).height(20.dp).quizzSkeleton())
        }
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = QuizzTvLayout.screenInset.dp,
                vertical = QuizzTvLayout.focusInset.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(QuizzTvLayout.itemGap.dp),
        ) {
            items(4, key = { "skeleton-card-$it" }) { QuizzTvSkeletonCard() }
        }
    }
}

@Composable
fun QuizzTvSkeletonCard(modifier: Modifier = Modifier) {
    Column(
        modifier.width(QuizzTvLayout.cardWidth.dp).border(
            BorderStroke(1.dp, MaterialTheme.colorScheme.border.copy(alpha = 0.16f)),
            MaterialTheme.shapes.medium,
        ),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).quizzSkeleton(MaterialTheme.shapes.medium))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(Modifier.height(48.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth(0.85f).height(20.dp).quizzSkeleton())
                Box(Modifier.fillMaxWidth(0.6f).height(20.dp).quizzSkeleton())
            }
            Box(Modifier.fillMaxWidth(0.7f).height(20.dp).quizzSkeleton())
            Column(Modifier.height(30.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.width(92.dp).height(14.dp).quizzSkeleton())
                Box(Modifier.fillMaxWidth(0.8f).height(3.dp).quizzSkeleton())
            }
        }
    }
}
