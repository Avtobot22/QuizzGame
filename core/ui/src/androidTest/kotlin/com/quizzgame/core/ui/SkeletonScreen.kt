package com.quizzgame.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.tags.SkeletonTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

internal const val SKELETON_LABEL = "Loading collections"

internal class SkeletonScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<SkeletonScreen>(provider) {
    private val loading = KNode(provider) { hasContentDescription(SKELETON_LABEL) }

    fun assertAccessibleLoadingWithoutFocusTargets() {
        loading.assertIsDisplayed()
        provider.onAllNodes(hasContentDescription(SKELETON_LABEL)).assertCountEquals(1)
        provider.onAllNodes(hasText("Decorative placeholder")).assertCountEquals(0)
        provider.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.RequestFocus)).assertCountEquals(0)
        provider.onNodeWithTag(SkeletonTags.ROOT).assert(
            SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate),
        )
    }

    fun frame() = provider.onNodeWithTag(SkeletonTags.ROOT).captureToImage().asAndroidBitmap()
}

internal fun ComposeContentTestRule.setSkeleton() {
    setContent {
        QuizzSkeleton(
            SKELETON_LABEL,
            Color(0xFF252B2C),
            Color(0xFF3A4142),
            Modifier.size(320.dp, 200.dp).background(Color(0xFF111415)).testTag(SkeletonTags.ROOT),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.width(280.dp).height(120.dp).quizzSkeleton())
                Box(Modifier.width(180.dp).height(20.dp).quizzSkeleton())
                BasicText("Decorative placeholder")
            }
        }
    }
}
