package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    require(length > 0)
    var focused by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.headlineSmall)
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { char -> char in '0'..'9' }.take(length)) },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .onFocusChanged { focused = it.isFocused }
                .semantics {
                    contentDescription = label
                    if (errorMessage != null) error(errorMessage)
                },
            textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                OtpDecoration(value, length, focused, enabled, errorMessage != null, innerTextField)
            },
        )
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
        modifier = modifier,
    )
}

@Composable
private fun OtpDecoration(
    value: String,
    length: Int,
    focused: Boolean,
    enabled: Boolean,
    isError: Boolean,
    innerTextField: @Composable () -> Unit,
) {
    val outline = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    Box {
        Box(
            Modifier
                .size(1.dp)
                .alpha(0f),
        ) { innerTextField() }
        Row(
            Modifier.clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(length) { index ->
                val active = focused && enabled && index == value.length.coerceAtMost(length - 1)
                val border = if (active && !isError) {
                    MaterialTheme.colorScheme.primary
                } else {
                    outline
                }
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .alpha(if (enabled) 1f else 0.38f)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.medium)
                        .border(
                            BorderStroke(if (active) 2.dp else 1.dp, border),
                            MaterialTheme.shapes.medium,
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        value.getOrNull(index)?.toString() ?: "—",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp,
                        ),
                    )
                }
            }
        }
    }
}
