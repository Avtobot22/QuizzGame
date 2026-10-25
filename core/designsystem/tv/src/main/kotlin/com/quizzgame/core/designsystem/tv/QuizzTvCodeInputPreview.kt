package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.quizzgame.core.ui.R
import com.quizzgame.core.ui.tokens.QuizzDimensions

@Preview(name = "TV numeric code", widthDp = 960, heightDp = 540, locale = "ru")
@Composable
private fun QuizzTvNumericCodeInputPreview() {
    CodeInputPreview(length = 4, characters = "1234567890")
}

@Preview(name = "TV alphanumeric code", widthDp = 960, heightDp = 540, locale = "ru")
@Composable
private fun QuizzTvAlphanumericCodeInputPreview() {
    CodeInputPreview(length = 6, characters = "ABCD1234")
}

@Composable
private fun CodeInputPreview(length: Int, characters: String) {
    var code by remember { mutableStateOf("") }
    QuizzTvTheme {
        Surface(
            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = QuizzDimensions.tvSafeHorizontalDp.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    QuizzDimensions.tvCodeKeyboardGapDp.dp,
                    Alignment.CenterVertically,
                ),
            ) {
                Text(stringResource(R.string.quizz_tv_code_label), style = MaterialTheme.typography.headlineLarge)
                QuizzTvCodeInput(
                    value = code,
                    onValueChange = { code = it },
                    onComplete = {},
                    length = length,
                    characters = characters,
                    label = stringResource(R.string.quizz_tv_code_label),
                    requestInitialFocus = false,
                )
            }
        }
    }
}
