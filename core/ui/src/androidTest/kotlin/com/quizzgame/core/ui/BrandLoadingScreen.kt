package com.quizzgame.core.ui

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import org.junit.Assert.assertTrue

internal const val LOADING_LABEL = "QuizzGame is loading"
private const val LOADER_TAG = "brand-loader"
private const val TITLE = "Decorative brand title"
private const val SUPPORTING = "Decorative supporting text"
private val background = Color(0xFF111415)

internal class BrandLoadingScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<BrandLoadingScreen>(provider) {
    val loading = KNode(provider) { hasContentDescription(LOADING_LABEL) }

    fun assertSingleStableAnnouncement() {
        loading.assertIsDisplayed()
        provider.onAllNodes(hasContentDescription(LOADING_LABEL)).assertCountEquals(1)
        provider.onAllNodes(hasText(TITLE)).assertCountEquals(0)
        provider.onAllNodes(hasText(SUPPORTING)).assertCountEquals(0)
        provider.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.RequestFocus)).assertCountEquals(0)
        provider.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Focused)).assertCountEquals(0)
        provider.onNodeWithTag(LOADER_TAG)
            .assertHasNoClickAction()
            .assert(
                SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(LOADING_LABEL)),
            )
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo.Indeterminate,
                ),
            )
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Text))
    }

    fun captureVisibleFrame(): Bitmap {
        val bitmap = provider.onNodeWithTag(LOADER_TAG).captureToImage().asAndroidBitmap()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        assertTrue("The finished loader must render visible content", pixels.any { it != background.toArgb() })
        return bitmap
    }
}

internal fun ComposeContentTestRule.setBrandLoading(size: QuizzBrandLoadingSize = QuizzBrandLoadingSize.COMPACT) {
    setContent {
        Box(Modifier.size(320.dp).background(background)) {
            QuizzBrandLoading(
                label = LOADING_LABEL,
                primary = Color(0xFF7FD5CC),
                accent = Color(0xFFB9E4D4),
                title = { BasicText(TITLE, style = TextStyle(color = Color.White, fontSize = 24.sp)) },
                supporting = { BasicText(SUPPORTING, style = TextStyle(color = Color.White, fontSize = 14.sp)) },
                size = size,
                modifier = Modifier.testTag(LOADER_TAG),
            )
        }
    }
}

internal class TestMotionDurationScale(override val scaleFactor: Float) : MotionDurationScale
