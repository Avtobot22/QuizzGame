package com.quizzgame.feature.play.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quizzgame.core.designsystem.mobile.QuizzInfoDialog
import com.quizzgame.feature.play.mobile.R
import com.quizzgame.feature.play.mobile.ui.tags.MobilePlayTags

@Composable
internal fun MobilePlayUnavailable(onDismiss: () -> Unit) {
    QuizzInfoDialog(
        title = stringResource(R.string.play_not_connected),
        message = stringResource(R.string.play_pending),
        closeLabel = stringResource(R.string.play_close),
        onDismiss = onDismiss,
        modifier = Modifier.testTag(MobilePlayTags.UNAVAILABLE),
    )
}
