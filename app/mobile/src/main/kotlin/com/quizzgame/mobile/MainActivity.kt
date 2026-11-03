package com.quizzgame.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.navigation.ProcessNavigationGuard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(ProcessNavigationGuard.restore(savedInstanceState))
        enableEdgeToEdge()
        setContent {
            QuizzMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) { MobileAppRoot() }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ProcessNavigationGuard.save(outState)
    }
}
