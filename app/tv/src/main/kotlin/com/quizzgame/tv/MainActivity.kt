package com.quizzgame.tv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import com.quizzgame.app.navigation.ExternalNavigationRouter
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.core.designsystem.tv.QuizzTvTheme
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.tv.navigation.DashboardNavigationContext
import com.quizzgame.tv.navigation.TvNavigationContext
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val rootViewModel: AppRootViewModel by viewModels()

    @Inject
    lateinit var externalNavigationRouter: ExternalNavigationRouter

    @Inject
    internal lateinit var navigationGraphs: Set<@JvmSuppressWildcards FeatureEntries<TvNavigationContext>>

    @Inject
    internal lateinit var dashboardGraphs: Set<@JvmSuppressWildcards FeatureEntries<DashboardNavigationContext>>

    @Inject
    lateinit var navigationPolicies: Set<@JvmSuppressWildcards NavigationPolicy>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) acceptNavigationIntent(intent)
        setContent {
            QuizzTvTheme {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    TvAppRoot(
                        navigationGraphs,
                        dashboardGraphs,
                        viewModel = rootViewModel,
                        policies = navigationPolicies,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptNavigationIntent(intent)
    }

    private fun acceptNavigationIntent(intent: Intent) {
        val uri = intent.dataString ?: return
        externalNavigationRouter.parse(uri)?.let(rootViewModel::requestNavigation)
    }
}
