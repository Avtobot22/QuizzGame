package com.quizzgame.core.designsystem.mobile

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.core.designsystem.mobile.ui.tags.MobileVisualTags
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode
import java.io.File

internal class MobileVisualScreen(private val provider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MobileVisualScreen>(provider) {
    fun text(label: String): KNode = KNode(provider) { hasText(label) }

    fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("visual-previews"))
        File(directory, name).outputStream().use { output ->
            val bitmap = provider.onNodeWithTag(MobileVisualTags.ROOT).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
}
