package com.quizzgame.feature.room.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileSkeleton
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.shared.room.R

@Composable
internal fun MobileLobbySkeleton(modifier: Modifier = Modifier) {
    val shapes = MaterialTheme.shapes
    QuizzMobileSkeleton(stringResource(R.string.room_connecting), modifier) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.fillMaxWidth().height(MobileLobbyHeroHeight).quizzSkeleton(shapes.extraLarge))
            MobileRosterHeading(count = null)
            repeat(3) {
                Box(Modifier.fillMaxWidth().height(MobileLobbyPlayerHeight).quizzSkeleton(shapes.large))
            }
        }
    }
}
