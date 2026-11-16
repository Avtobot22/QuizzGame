package com.quizzgame.core.designsystem.mobile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.preview.QuizzMobilePreviewScreen
import com.quizzgame.core.designsystem.mobile.ui.tags.MobileVisualTags
import com.quizzgame.core.ui.status.QuizzStatusIcon
import org.junit.Rule
import org.junit.Test

class QuizzMobileVisualLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val screen get() = MobileVisualScreen(compose)

    @Test
    fun roomStateUsesTheWholeViewportAndKeepsItsCopyVisible() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                QuizzMobilePreviewScreen(Modifier.size(390.dp, 760.dp).testTag(MobileVisualTags.ROOT)) {
                    QuizzMessageScreen(
                        "Большой экран. Ваша компания.",
                        "Выберите квиз на телевизоре и подключайтесь с телефона.",
                        icon = QuizzStatusIcon.ROOM,
                    )
                }
            }
        }
        screen.text("Большой экран. Ваша компания.").assertIsDisplayed()
        screen.capture("mobile-room.png")
    }

    @Test
    fun narrowFormsKeepLabelsAndRecoveryTextVisibleAtLargeFontScale() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.6f)) {
                QuizzMobilePreviewScreen(Modifier.size(320.dp, 760.dp).testTag(MobileVisualTags.ROOT)) {
                    Column(
                        Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        QuizzOtpInput("12", {}, "Код подтверждения", Modifier.fillMaxWidth())
                        QuizzRoomCodeField(
                            "123",
                            {},
                            "Код комнаты",
                            Modifier.fillMaxWidth(),
                            errorMessage = "Проверьте код на телевизоре",
                        )
                        QuizzButton({}) { Text("Продолжить", maxLines = 1) }
                    }
                }
            }
        }
        screen.text("Проверьте код на телевизоре").assertIsDisplayed()
        screen.capture("mobile-forms-large-font.png")
    }
}
