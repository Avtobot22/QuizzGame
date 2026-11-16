package com.quizzgame.feature.quizentry.tv

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.quizzgame.core.designsystem.tv.preview.QuizzTvPreviewScreen
import com.quizzgame.feature.quizentry.tv.ui.content.TvQuizEntryStateContent
import com.quizzgame.feature.quizentry.tv.ui.preview.TvQuizEntryPreviewProvider
import com.quizzgame.feature.quizentry.tv.ui.tags.TvVisualTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class TvQuizEntryVisualTest(private val index: Int) {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun stateFitsTheTvViewport() {
        val state = TvQuizEntryPreviewProvider().values.elementAt(index)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzTvPreviewScreen(Modifier.size(960.dp, 540.dp).testTag(TvVisualTags.ROOT)) {
                    TvQuizEntryStateContent(state, {}, {}, Modifier, {})
                }
            }
        }
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("visual-previews"))
        File(directory, "quiz-entry-$index.png").outputStream().use { output ->
            val bitmap = compose.onNodeWithTag(TvVisualTags.ROOT).captureToImage().asAndroidBitmap()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "state {0}")
        fun states(): List<Array<Any>> = TvQuizEntryPreviewProvider().values.mapIndexed { index, _ ->
            arrayOf<Any>(index)
        }.toList()
    }
}
