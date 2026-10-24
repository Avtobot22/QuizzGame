package com.quizzgame.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.ui.R
import com.quizzgame.core.ui.WelcomeContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TvApp() }
    }
}

@Composable
private fun TvApp() {
    val initialFocus = remember { FocusRequester() }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding().padding(horizontal = 48.dp, vertical = 24.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            WelcomeContent(
                heading = {
                    Text(
                        text = stringResource(R.string.quizz_title),
                        modifier = Modifier.semantics { heading() },
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displayLarge,
                    )
                },
                description = {
                    Text(
                        text = stringResource(R.string.quizz_tv_description),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                action = { expanded, onClick ->
                    val label = if (expanded) R.string.quizz_hide_instructions else R.string.quizz_show_instructions
                    Button(onClick = onClick, modifier = Modifier.focusRequester(initialFocus)) {
                        Text(stringResource(label))
                    }
                },
                instructions = {
                    Text(
                        text = stringResource(R.string.quizz_instructions),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                modifier = Modifier.widthIn(max = 800.dp),
            )
        }
    }

    LaunchedEffect(initialFocus) { initialFocus.requestFocus() }
}

@Preview(device = "id:tv_1080p", showBackground = true)
@Composable
private fun TvAppPreview() {
    TvApp()
}
