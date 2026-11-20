package com.quizzgame.feature.quizentry.mobile

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.feature.quizentry.mobile.ui.tags.MobileQuizEntryTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import java.io.File

internal class MobileQuizEntryVisualScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MobileQuizEntryVisualScreen>(provider) {
    fun bounds(tag: String): Rect = provider.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
    fun tag(value: String) = KNode(provider) { hasTestTag(value) }
    fun text(value: String) = KNode(provider) { hasText(value) }
    fun progress() = provider.onNodeWithTag(MobileQuizEntryTags.PROGRESS)
        .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
    fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("visual-previews"))
        File(directory, name).outputStream().use { output ->
            val bitmap = provider.onNodeWithTag(ROOT).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
    companion object { const val ROOT = "mobile-entry-visual-root" }
}
