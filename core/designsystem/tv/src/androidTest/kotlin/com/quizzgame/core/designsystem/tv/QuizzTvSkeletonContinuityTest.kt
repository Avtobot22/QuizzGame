package com.quizzgame.core.designsystem.tv

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.core.designsystem.tv.ui.tags.TvVisualTags
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import org.junit.Assert.assertArrayEquals
import org.junit.Rule
import org.junit.Test

class QuizzTvSkeletonContinuityTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>(effectContext = StaticSkeletonMotion)

    @Test
    fun revealingDashboardNavigationDoesNotMoveTheCatalogSkeleton() {
        val dashboardReady = mutableStateOf(false)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzTvPreviewScreen(Modifier.size(960.dp, 540.dp).testTag(TvVisualTags.ROOT)) {
                    if (dashboardReady.value) {
                        Column {
                            QuizzTvNavigationBar(
                                listOf("Home", "Library", "Settings"),
                                "Home",
                                {},
                                label = { QuizzTvActionLabel(it) },
                                modifier = Modifier.padding(
                                    horizontal = QuizzTvLayout.screenInset.dp,
                                    vertical = QuizzTvLayout.verticalInset.dp,
                                ),
                                trailingContent = {
                                    QuizzTvButton({}, Modifier.width(QuizzTvLayout.navigationActionWidth.dp)) {
                                        QuizzTvActionLabel("Sign out")
                                    }
                                },
                            )
                            QuizzTvCatalogSkeleton("Loading", Modifier.weight(1f))
                        }
                    } else {
                        QuizzTvCatalogSkeleton("Loading", showNavigation = true)
                    }
                }
            }
        }
        val screen = TvVisualScreen(compose)
        val before = screen.frame()
        compose.runOnIdle { dashboardReady.value = true }
        val after = screen.frame()
        val top = (QuizzTvLayout.verticalInset * 2 + QuizzTvLayout.buttonHeight).toInt()
        val first = IntArray(before.width * (before.height - top))
        val second = IntArray(first.size)
        before.getPixels(first, 0, before.width, 0, top, before.width, before.height - top)
        after.getPixels(second, 0, after.width, 0, top, after.width, after.height - top)
        assertArrayEquals("Loading rows must stay in place when the bar appears", first, second)
    }
}

private object StaticSkeletonMotion : MotionDurationScale {
    override val scaleFactor = 0f
}
