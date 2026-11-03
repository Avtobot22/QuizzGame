package com.quizzgame.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.navigation.ProcessNavigationGuard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(ProcessNavigationGuard.restore(savedInstanceState))
        enableEdgeToEdge()
        setContent {
            QuizzTvTheme {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { TvAppRoot() }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ProcessNavigationGuard.save(outState)
    }
}
