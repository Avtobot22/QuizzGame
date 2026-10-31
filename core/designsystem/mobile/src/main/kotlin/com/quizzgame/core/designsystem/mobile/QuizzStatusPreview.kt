package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon

@Preview(name = "Status and notices", locale = "ru", widthDp = 360)
@Composable
private fun StatusPreview() {
    QuizzMobileTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            QuizzStatusContent(icon = QuizzStatusIcon.GENERIC) { Text("Пока ничего нет") }
            QuizzNoticeBanner(tone = QuizzNoticeTone.INFORMATION) { Text("Данные обновлены") }
            QuizzInlineMessage(tone = QuizzNoticeTone.ERROR) { Text("Проверьте введённое значение") }
        }
    }
}

@Preview(name = "Confirmation", locale = "ru", widthDp = 360)
@Composable
private fun ConfirmationPreview() {
    QuizzMobileTheme {
        QuizzConfirmationDialog(
            title = "Подтвердите действие",
            text = "Продолжить выполнение?",
            onConfirm = {},
            onDismissRequest = {},
        )
    }
}
