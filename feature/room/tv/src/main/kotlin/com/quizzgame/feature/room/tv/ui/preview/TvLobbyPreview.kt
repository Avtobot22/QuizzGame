package com.quizzgame.feature.room.tv.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.room.tv.presentation.TvLobbyState
import com.quizzgame.feature.room.tv.ui.TvLobbyContent

@Preview(name = "Lobby", locale = "ru", widthDp = 960, heightDp = 540)
@Composable
private fun LobbyPreview(@PreviewParameter(TvLobbyPreviewProvider::class) state: TvLobbyState) {
    QuizzTvPreviewScreen { TvLobbyContent(state, {}, {}) }
}
