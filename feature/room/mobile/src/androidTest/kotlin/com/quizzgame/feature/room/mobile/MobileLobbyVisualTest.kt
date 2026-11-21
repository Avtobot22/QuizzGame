package com.quizzgame.feature.room.mobile

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.feature.room.mobile.ui.MobileLobbyContent
import com.quizzgame.feature.room.mobile.ui.preview.MobileLobbyPreviewProvider
import com.quizzgame.feature.room.mobile.ui.tags.MobileLobbyTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class MobileLobbyVisualTest(private val index: Int) {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun capturesLobbyState() {
        val state = MobileLobbyPreviewProvider().values.elementAt(index)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzMobilePreviewScreen(Modifier.size(390.dp, 820.dp)) {
                    MobileLobbyContent(state, {}, {}, {})
                }
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("visual-previews"))
        File(directory, "mobile-lobby-$index.png").outputStream().use { output ->
            val bitmap = compose.onNodeWithTag(MobileLobbyTags.ROOT).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "state {0}")
        fun states(): List<Array<Any>> = MobileLobbyPreviewProvider().values.mapIndexed { index, _ ->
            arrayOf<Any>(index)
        }.toList()
    }
}
