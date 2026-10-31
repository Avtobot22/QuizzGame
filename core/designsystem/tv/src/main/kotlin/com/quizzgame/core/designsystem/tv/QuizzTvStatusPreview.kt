package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.quizzgame.core.ui.status.QuizzNoticeTone
import com.quizzgame.core.ui.status.QuizzStatusIcon

@Preview(name = "Status and notices", locale = "ru", widthDp = 960)
@Composable
private fun StatusPreview() {
    QuizzTvTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            QuizzTvStatusContent(icon = QuizzStatusIcon.GENERIC) { Text("Пока ничего нет") }
            QuizzTvNoticeBanner(tone = QuizzNoticeTone.INFORMATION) { Text("Данные обновлены") }
            QuizzTvInlineMessage(tone = QuizzNoticeTone.ERROR) { Text("Проверьте введённое значение") }
        }
    }
}

@Preview(name = "Confirmation", locale = "ru", widthDp = 960)
@Composable
private fun ConfirmationPreview() {
    QuizzTvTheme {
        QuizzTvConfirmationDialog(
            title = "Подтвердите действие",
            text = "Продолжить выполнение?",
            onConfirm = {},
            onDismissRequest = {},
        )
    }
}
