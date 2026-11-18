package com.quizzgame.feature.play.mobile.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState

internal class MobilePlayPreviewProvider : PreviewParameterProvider<MobilePlayState> {
    override val values = MobilePlayState.entries.asSequence()
}
