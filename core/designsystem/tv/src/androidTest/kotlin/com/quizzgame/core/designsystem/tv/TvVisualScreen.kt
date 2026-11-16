package com.quizzgame.core.designsystem.tv

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.core.designsystem.tv.ui.tags.TvVisualTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.io.File

internal class TvVisualScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<TvVisualScreen>(provider) {
    fun action(label: String): KNode = KNode(provider) {
        hasText(label)
        hasClickAction()
    }

    fun assertSingleLine(label: String) {
        val results = mutableListOf<TextLayoutResult>()
        provider.onNodeWithText(label, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(1, results.single().lineCount)
        assertFalse(results.single().hasVisualOverflow)
    }

    fun assertSeparated(firstTag: String, secondTag: String) {
        val first = provider.onNodeWithTag(firstTag).fetchSemanticsNode().boundsInRoot
        val second = provider.onNodeWithTag(secondTag).fetchSemanticsNode().boundsInRoot
        assertTrue(first.right < second.left)
    }

    fun assertCentered(label: String) {
        val root = provider.onNodeWithTag(TvVisualTags.ROOT).fetchSemanticsNode().boundsInRoot
        val action = provider.onNodeWithText(label).fetchSemanticsNode().boundsInRoot
        assertEquals(root.center.x, action.center.x, 1f)
        assertTrue(action.bottom < root.bottom)
    }

    fun assertActionContentCentered(label: String) {
        val action = provider.onNodeWithText(label).fetchSemanticsNode().boundsInRoot
        val text = provider.onNodeWithText(label, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertEquals(action.center.y, text.center.y, 1f)
    }

    fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("visual-previews"))
        File(directory, name).outputStream().use { output ->
            val bitmap = provider.onNodeWithTag(TvVisualTags.ROOT).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    fun frame() = provider.onNodeWithTag(TvVisualTags.ROOT).captureToImage().asAndroidBitmap()

    fun controlFill(tag: String): Int {
        val bounds = provider.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        return frame().getPixel(bounds.center.x.toInt(), (bounds.top + 6).toInt())
    }
}
