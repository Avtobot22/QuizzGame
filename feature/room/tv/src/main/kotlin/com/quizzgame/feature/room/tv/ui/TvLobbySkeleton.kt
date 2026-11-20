package com.quizzgame.feature.room.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.designsystem.tv.QuizzTvSkeleton
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.shared.room.R

@Composable
internal fun TvLobbySkeleton(modifier: Modifier = Modifier) {
    val shapes = MaterialTheme.shapes
    QuizzTvSkeleton(stringResource(R.string.room_connecting), modifier) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.weight(TV_LOBBY_INVITE_WEIGHT).fillMaxHeight().quizzSkeleton(shapes.extraLarge))
            Column(
                Modifier.weight(TV_LOBBY_ROSTER_WEIGHT),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    TvRosterHeading(count = null)
                    LazyVerticalGrid(
                        columns = TvLobbyPlayerColumns,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        userScrollEnabled = false,
                    ) {
                        items(listOf(0, 1, 2, 3)) {
                            Box(Modifier.fillMaxWidth().height(TvLobbyPlayerHeight).quizzSkeleton(shapes.medium))
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(TvLobbyReadinessHeight).quizzSkeleton(shapes.large))
            }
        }
    }
}
