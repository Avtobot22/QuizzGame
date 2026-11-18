package com.quizzgame.feature.play.mobile

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import java.io.File

internal class MobilePlayVisualScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MobilePlayVisualScreen>(provider) {
    fun tag(value: String) = KNode(provider) { hasTestTag(value) }
    fun bounds(tag: String): Rect = provider.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
    fun capture(tag: String, name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("visual-previews"))
        File(directory, name).outputStream().use { output ->
            val bitmap = provider.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
    companion object { const val ROOT = "mobile-play-visual-root" }
}
