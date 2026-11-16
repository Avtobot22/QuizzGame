package com.quizzgame.tv

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.quizzgame.app.navigation.AppNavigationConfiguration
import com.quizzgame.app.navigation.AppNavigationEnvironment
import com.quizzgame.app.navigation.AppNavigationGraph
import com.quizzgame.app.navigation.NavigationCoordinator
import com.quizzgame.core.navigation.policy.DestinationAccess
import com.quizzgame.core.navigation.policy.NavigationPolicy
import com.quizzgame.core.navigation.registration.FeatureEntries
import com.quizzgame.core.session.lifecycle.SessionState
import com.quizzgame.tv.ui.tags.UserContextTags
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Rule
import org.junit.Test
import com.quizzgame.tv.test.R as FixtureR

class UserContextNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private var session by mutableStateOf<SessionState.Ready>(SessionState.Authenticated(1))
    private lateinit var coordinator: NavigationCoordinator
    private val models = mutableMapOf<Key, UserContextModel>()

    @Test
    fun accountSwitchAndLogoutClearActiveAndInactiveViewModelsAndCancelTheirWork() {
        compose.setContent { Graph() }
        await(Key.HOME)
        listOf(SessionState.Authenticated(2), SessionState.Guest).forEach { next ->
            val oldHome = compose.runOnIdle { models.getValue(Key.HOME).also { it.increment() } }
            compose.runOnIdle { coordinator.navigator.switchTopLevel(Key.LIBRARY) }
            await(Key.LIBRARY)
            val oldLibrary = compose.runOnIdle { models.getValue(Key.LIBRARY).also { it.increment() } }
            compose.runOnIdle {
                assertFalse(oldHome.cleared)
                assertEquals(1, oldHome.counter)
                session = next
            }
            await(Key.HOME)
            compose.waitUntil(5_000) { oldHome.workCancelled && oldLibrary.workCancelled }
            compose.runOnIdle {
                assertNotSame(oldHome, models.getValue(Key.HOME))
                assertEquals(0, models.getValue(Key.HOME).counter)
                coordinator.navigator.switchTopLevel(Key.LIBRARY)
            }
            await(Key.LIBRARY)
            compose.runOnIdle {
                assertNotSame(oldLibrary, models.getValue(Key.LIBRARY))
                assertEquals(0, models.getValue(Key.LIBRARY).counter)
                coordinator.navigator.switchTopLevel(Key.HOME)
            }
            await(Key.HOME)
        }
    }

    @Composable
    private fun Graph() {
        val current = session
        val owner = when (current) {
            is SessionState.Authenticated -> "account:${current.sessionGeneration}"
            SessionState.Guest -> "guest"
            SessionState.Expired -> "expired"
        }
        val graphs = remember {
            setOf(
                FeatureEntries<NavigationCoordinator> { scope, context ->
                    scope.entry<Key> { route ->
                        when (route) {
                            Key.SPLASH -> LaunchedEffect(Unit) { context.value.splashCompleted() }
                            Key.AUTH -> Box {}
                            Key.HOME, Key.LIBRARY -> Content(route)
                        }
                    }
                },
            )
        }
        AppNavigationGraph(
            AppNavigationConfiguration(
                Key.HOME,
                Key.SPLASH,
                Key.AUTH,
                topLevels = listOf(Key.HOME, Key.LIBRARY),
                policy = NavigationPolicy { DestinationAccess.PUBLIC },
            ),
            AppNavigationEnvironment(owner, owner, { session }),
            Key.SPLASH,
            graphs,
            context = { it.also { coordinator = it } },
        )
    }

    @Composable
    private fun Content(key: Key, model: UserContextModel = viewModel { UserContextModel() }) {
        SideEffect { models[key] = model }
        BasicText(
            stringResource(FixtureR.string.fixture_counter, model.counter),
            Modifier.testTag(UserContextTags.screen(key)),
        )
    }

    private fun await(key: Key) {
        compose.waitUntil(5_000) { compose.onNodeWithTag(UserContextTags.screen(key)).isDisplayed() }
        AppShellScreen(compose).tag(UserContextTags.screen(key)).assertIsDisplayed()
    }

    @Serializable
    enum class Key : NavKey { HOME, LIBRARY, SPLASH, AUTH }
}

internal class UserContextModel : ViewModel() {
    var counter by mutableIntStateOf(0)
        private set

    @Volatile
    var cleared = false
        private set

    @Volatile
    var workCancelled = false
        private set

    init {
        viewModelScope.launch {
            try {
                awaitCancellation()
            } finally {
                workCancelled = true
            }
        }
    }

    fun increment() { counter++ }
    override fun onCleared() { cleared = true }
}
