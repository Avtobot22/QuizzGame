package com.quizzgame.feature.quizentry.tv.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import com.quizzgame.core.designsystem.tv.QuizzTvActionLabel
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.designsystem.tv.QuizzTvButtonStyle
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.core.error.ui.common.R as ErrorR
import com.quizzgame.core.ui.R as UiR

@Composable
internal fun TvQuizEntryBack(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val focus = remember { FocusRequester() }
    QuizzTvButton(onBack, modifier.focusRequester(focus), style = QuizzTvButtonStyle.SECONDARY) {
        Icon(painterResource(UiR.drawable.ic_quizz_back), null, Modifier.size(18.dp))
        QuizzTvActionLabel(
            stringResource(ErrorR.string.quizz_error_action_go_back),
            Modifier.padding(start = 8.dp),
        )
    }
    TvEntryFocus(focus)
}
