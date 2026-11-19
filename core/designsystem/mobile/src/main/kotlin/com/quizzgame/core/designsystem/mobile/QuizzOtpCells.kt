package com.quizzgame.core.designsystem.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Единое поле с ячейками для цифрового кода. Подпись и способ ввода задаёт экран. */
@Composable
fun QuizzOtpCells(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true,
    errorMessage: String? = null,
    autofill: ContentType? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    require(length > 0)
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { char -> char in '0'..'9' }.take(length)) },
        modifier = modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }.semantics {
            contentDescription = label
            if (autofill != null) contentType = autofill
            errorMessage?.let { error(it) }
        },
        singleLine = true,
        enabled = enabled,
        textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { input ->
            Box(contentAlignment = Alignment.CenterStart) {
                Row(
                    Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.38f).clearAndSetSemantics { },
                    horizontalArrangement = Arrangement.spacedBy(if (length > 6) 4.dp else 8.dp),
                ) {
                    repeat(length) { index ->
                        CodeCell(
                            value.getOrNull(index),
                            focused && enabled && index == value.length.coerceAtMost(length - 1),
                            errorMessage != null,
                            Modifier.weight(1f),
                        )
                    }
                }
                Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterStart) { input() }
            }
        },
    )
}

@Composable
private fun CodeCell(digit: Char?, active: Boolean, invalid: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    val stroke = when {
        invalid -> colors.error
        active  -> colors.primary
        else    -> colors.outlineVariant
    }
    Box(
        modifier.heightIn(min = 56.dp)
            .background(
                if (active) colors.primaryContainer.copy(alpha = 0.45f) else colors.surfaceContainerLowest,
                shape,
            )
            .border(if (active) 2.dp else 1.dp, stroke, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (digit != null) {
            Text(digit.toString(), style = MaterialTheme.typography.titleLarge)
        } else {
            Box(Modifier.size(width = 12.dp, height = 2.dp).background(stroke, shape))
        }
    }
}
