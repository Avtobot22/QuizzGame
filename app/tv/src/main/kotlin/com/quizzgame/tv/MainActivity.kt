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
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.navigation.DashboardNavigationContext
import com.quizzgame.tv.navigation.TvNavigationContext
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    internal lateinit var navigationGraphs: Set<@JvmSuppressWildcards FeatureEntries<TvNavigationContext>>

    @Inject
    internal lateinit var dashboardGraphs: Set<@JvmSuppressWildcards FeatureEntries<DashboardNavigationContext>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizzTvTheme {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    TvAppRoot(navigationGraphs, dashboardGraphs)
                }
            }
        }
    }
}
