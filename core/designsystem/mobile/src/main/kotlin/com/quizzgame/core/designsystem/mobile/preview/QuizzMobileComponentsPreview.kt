package com.quizzgame.core.designsystem.mobile.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.core.designsystem.mobile.QuizzOtpInput
import com.quizzgame.core.designsystem.mobile.QuizzRoomCodeField
import com.quizzgame.core.designsystem.mobile.QuizzScreenScaffold
import com.quizzgame.core.designsystem.mobile.QuizzTopBar

@Preview(name = "Mobile components", locale = "ru", widthDp = 390, heightDp = 844)
@Composable
private fun ComponentsPreview() {
    QuizzMobilePreviewScreen {
        QuizzScreenScaffold(topBar = { QuizzTopBar("Quizz") }) { padding ->
            Column(
                Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Text("Играем вместе", style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuizzButton({}) { Text("Продолжить", maxLines = 1) }
                    QuizzButton({}, style = QuizzButtonStyle.TONAL) { Text("Назад", maxLines = 1) }
                }
                QuizzRoomCodeField("QU12", {}, "Код комнаты", Modifier.fillMaxWidth())
                QuizzOtpInput("12", {}, "Код подтверждения", Modifier.fillMaxWidth())
                QuizzRoomCodeField(
                    "123",
                    {},
                    "Код комнаты",
                    Modifier.fillMaxWidth(),
                    errorMessage = "Проверьте код на экране телевизора",
                )
            }
        }
    }
}
