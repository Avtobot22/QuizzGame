package com.quizzgame.core.designsystem.mobile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizzScreenScaffoldTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shellConsumedNavigationInsetIsNotAddedAgainByTheFeatureScaffold() {
        lateinit var received: PaddingValues
        compose.setContent {
            QuizzMobileTheme {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).consumeWindowInsets(WindowInsets(bottom = 40))) {
                        QuizzScreenScaffold(contentWindowInsets = WindowInsets(top = 24, bottom = 40)) { padding ->
                            SideEffect { received = padding }
                            Box(Modifier.fillMaxSize().padding(padding))
                        }
                    }
                    Box(Modifier.height(80.dp))
                }
            }
        }
        compose.runOnIdle {
            with(compose.density) {
                assertEquals(24.toDp(), received.calculateTopPadding())
                assertEquals(0.dp, received.calculateBottomPadding())
            }
        }
    }

    @Test
    @OptIn(ExperimentalMaterial3Api::class)
    fun featureOwnsItsTopBarScrollBehaviorAndRemainingKeyboardInset() {
        lateinit var received: PaddingValues
        compose.setContent {
            QuizzMobileTheme {
                val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                QuizzScreenScaffold(
                    modifier = Modifier.nestedScroll(behavior.nestedScrollConnection),
                    topBar = { TopAppBar(title = { Text("Feature title") }, scrollBehavior = behavior) },
                    contentWindowInsets = WindowInsets(bottom = 240),
                ) { padding ->
                    SideEffect { received = padding }
                    Box(Modifier.fillMaxSize().padding(padding))
                }
            }
        }
        compose.runOnIdle {
            with(compose.density) { assertEquals(240.toDp(), received.calculateBottomPadding()) }
            org.junit.Assert.assertTrue(received.calculateTopPadding() > 0.dp)
        }
    }
}
