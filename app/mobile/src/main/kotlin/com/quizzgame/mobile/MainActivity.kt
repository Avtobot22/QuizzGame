package com.quizzgame.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quizzgame.app.navigation.ExternalNavigationRouter
import com.quizzgame.app.presentation.AppRootViewModel
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.mobile.navigation.MobileNavigationContext
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val rootViewModel: AppRootViewModel by viewModels()

    @Inject
    lateinit var externalNavigationRouter: ExternalNavigationRouter

    @Inject
    internal lateinit var navigationGraphs: Set<@JvmSuppressWildcards FeatureEntries<MobileNavigationContext>>

    @Inject
    lateinit var navigationPolicies: Set<@JvmSuppressWildcards NavigationPolicy>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) acceptNavigationIntent(intent)
        setContent {
            QuizzMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) { MobileAppRoot(navigationGraphs, viewModel = rootViewModel, policies = navigationPolicies) }
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
