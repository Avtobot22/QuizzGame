package com.quizzgame.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.mobile.navigation.MobileNavigationContext
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    internal lateinit var navigationGraphs: Set<@JvmSuppressWildcards FeatureEntries<MobileNavigationContext>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizzMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) { MobileAppRoot(navigationGraphs) }
            }
        }
    }
}
