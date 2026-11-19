package com.quizzgame.feature.account.tv.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.designsystem.tv.QuizzTvSkeleton
import com.quizzgame.core.ui.QuizzProfilePanel
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.account.tv.R

@Composable
internal fun TvProfileLoading() {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    QuizzTvSkeleton(stringResource(R.string.profile_loading), Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            QuizzProfilePanel(
                colors.surfaceVariant.copy(alpha = 0.35f),
                colors.primaryContainer,
                shapes.extraLarge,
            ) {
                Box(Modifier.size(76.dp).quizzSkeleton(CircleShape))
                Box(Modifier.fillMaxWidth(0.38f).height(26.dp).quizzSkeleton())
                Box(Modifier.fillMaxWidth(0.63f).height(20.dp).quizzSkeleton())
            }
            Box(Modifier.fillMaxWidth(0.35f).height(QuizzTvLayout.buttonHeight.dp).quizzSkeleton(shapes.medium))
        }
    }
}
