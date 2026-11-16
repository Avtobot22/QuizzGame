package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.core.ui.tokens.QuizzTvLayout

@Composable
fun QuizzTvDetailSkeleton(label: String, modifier: Modifier = Modifier) {
    QuizzTvSkeleton(label, modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.align(Alignment.CenterEnd).padding(end = QuizzTvLayout.screenInset.dp)
                    .fillMaxWidth(0.35f).fillMaxHeight(0.85f).quizzSkeleton(),
            )
            Column(Modifier.fillMaxSize().padding(QuizzTvLayout.screenInset.dp)) {
                Spacer(
                    Modifier.height(
                        (
                            QuizzTvLayout.buttonHeight + QuizzTvLayout.detailHeaderGap +
                                QuizzTvLayout.badgeHeight + 16f
                            ).dp,
                    ),
                )
                Column(Modifier.fillMaxWidth(0.6f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.fillMaxWidth(0.85f).height(42.dp).quizzSkeleton())
                        Box(Modifier.fillMaxWidth(0.65f).height(24.dp).quizzSkeleton())
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(2) { Box(Modifier.fillMaxWidth().height(20.dp).quizzSkeleton()) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(3) { Box(Modifier.width(92.dp).height(20.dp).quizzSkeleton()) }
                    }
                    Box(
                        Modifier.padding(
                            top = 8.dp,
                        ).width(168.dp).height(QuizzTvLayout.buttonHeight.dp).quizzSkeleton(),
                    )
                    Box(Modifier.width(200.dp).height(16.dp).quizzSkeleton())
                }
            }
        }
    }
}
