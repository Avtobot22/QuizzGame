package com.quizzgame.feature.room.mobile.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.room.mobile.ui.MobileLobbyContent
import com.quizzgame.shared.room.presentation.RoomLobbyState

@Preview(name = "Lobby", locale = "ru", widthDp = 390, heightDp = 820)
@Preview(name = "Lobby dark", locale = "en", widthDp = 360, heightDp = 800, uiMode = 32, fontScale = 1.5f)
@Composable
private fun LobbyPreview(@PreviewParameter(MobileLobbyPreviewProvider::class) state: RoomLobbyState) {
    QuizzMobilePreviewScreen { MobileLobbyContent(state, {}, {}, {}) }
}
