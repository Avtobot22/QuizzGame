package com.quizzgame.feature.play.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar
import com.quizzgame.feature.play.mobile.R
import com.quizzgame.feature.play.mobile.presentation.MobilePlayIntent
import com.quizzgame.feature.play.mobile.presentation.MobilePlayState
import com.quizzgame.feature.play.mobile.ui.tags.MobilePlayTags

@Composable
internal fun MobilePlayContent(
    state: MobilePlayState,
    onIntent: (MobilePlayIntent) -> Unit,
    modifier: Modifier = Modifier,
    onScan: () -> Unit = {},
    onSignIn: () -> Unit = {},
) {
    QuizzScreenScaffold(modifier.testTag(MobilePlayTags.ROOT), topBar = {
        QuizzTopBar(stringResource(R.string.play_title))
    }) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val wide = maxWidth >= 720.dp
            Column(
                Modifier.fillMaxSize().verticalScroll(
                    rememberScrollState(),
                ).padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (wide) {
                    Row(Modifier.widthIn(max = 1040.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        MobilePlayIntro(Modifier.weight(1f))
                        MobilePlayConnection(
                            state,
                            onIntent,
                            onScan,
                            onSignIn,
                            Modifier.weight(1f).padding(top = 24.dp),
                        )
                    }
                } else {
                    MobilePlayIntro(Modifier.widthIn(max = 440.dp).fillMaxWidth())
                    MobilePlayConnection(
                        state,
                        onIntent,
                        onScan,
                        onSignIn,
                        Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                    )
                }
            }
        }
    }
}
