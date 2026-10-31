package com.quizzgame.core.error.ui.mobile

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

internal fun captureErrorPreview(node: SemanticsNodeInteraction, fileName: String) {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val directory = checkNotNull(context.getExternalFilesDir("error-previews"))
    File(directory, fileName).outputStream().use { output ->
        check(node.captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output))
    }
}
