package com.quizzgame.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.R
import com.quizzgame.core.ui.WelcomeContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MobileApp() }
    }
}

@Composable
private fun MobileApp() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.safeDrawingPadding(), contentAlignment = Alignment.Center) {
                WelcomeContent(
                    heading = {
                        Text(
                            text = stringResource(R.string.quizz_title),
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.displayLarge,
                        )
                    },
                    description = {
                        Text(
                            stringResource(R.string.quizz_mobile_description),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    action = { expanded, onClick ->
                        val label = if (expanded) R.string.quizz_hide_instructions else R.string.quizz_show_instructions
                        Button(onClick = onClick) {
                            Text(stringResource(label))
                        }
                    },
                    instructions = { Text(stringResource(R.string.quizz_instructions)) },
                    modifier = Modifier.widthIn(max = 600.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MobileAppPreview() {
    MobileApp()
}
