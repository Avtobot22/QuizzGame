package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun QuizzOtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorMessage: String? = null,
    length: Int = 6,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        QuizzOtpCells(value, onValueChange, label, length = length, enabled = enabled, errorMessage = errorMessage)
        if (errorMessage != null) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun QuizzRoomCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorMessage: String? = null,
    supportingText: String? = null,
    length: Int = 6,
) {
    require(length > 0)
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(
                input.filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }
                    .uppercase(Locale.ROOT).take(length),
            )
        },
        label = { Text(label) },
        enabled = enabled,
        isError = errorMessage != null,
        supportingText = (errorMessage ?: supportingText)?.let { text -> { Text(text) } },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        modifier = modifier,
    )
}
