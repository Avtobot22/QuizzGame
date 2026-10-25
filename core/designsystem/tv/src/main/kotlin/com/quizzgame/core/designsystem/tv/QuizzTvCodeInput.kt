package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.quizzgame.core.ui.R
import com.quizzgame.core.ui.tokens.QuizzDimensions
import android.view.KeyEvent as AndroidKeyEvent

@Composable
fun QuizzTvCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    onComplete: (String) -> Unit,
    length: Int,
    characters: String,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    requestInitialFocus: Boolean = true,
) {
    require(length > 0)
    require(characters.isNotEmpty())
    val code = value.filter { it in characters }.take(length)
    val edit = rememberCodeEdit(code, length, characters, enabled, onValueChange, onComplete)
    val progress = stringResource(R.string.quizz_tv_code_progress, code.length, length)
    val errorText = errorMessage ?: stringResource(R.string.quizz_tv_code_error)
    Column(
        modifier = modifier
            .onPreviewKeyEvent { handleCodeHardwareKey(it, characters, enabled, edit) }
            .semantics {
                contentDescription = label
                stateDescription = progress
                if (!enabled) disabled()
                if (isError) error(errorText)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(QuizzDimensions.tvCodeKeyboardGapDp.dp),
    ) {
        CodeCells(code, length, enabled, isError)
        key(characters) { CodeKeyboard(characters, enabled, requestInitialFocus, edit) }
        if (isError) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Composable
private fun rememberCodeEdit(
    value: String,
    length: Int,
    characters: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onComplete: (String) -> Unit,
): (Char?) -> Unit {
    val pending = remember(length, characters) { CodeEditBuffer(value) }
    SideEffect { pending.value = value }
    return { character ->
        if (enabled) {
            val current = pending.value
            val next = if (character == null) current.dropLast(1) else (current + character).take(length)
            if (next != current) {
                pending.value = next
                onValueChange(next)
                if (next.length == length) onComplete(next)
            }
        }
    }
}

private class CodeEditBuffer(var value: String)

@Composable
private fun CodeCells(value: String, length: Int, enabled: Boolean, isError: Boolean) {
    val colors = MaterialTheme.colorScheme
    val scroll = rememberScrollState()
    val cellStepPx = with(LocalDensity.current) {
        (QuizzDimensions.tvCodeCellWidthDp + QuizzDimensions.tvCodeCellGapDp).dp.roundToPx()
    }
    LaunchedEffect(value.length, length) {
        scroll.scrollTo(cellStepPx * value.length)
    }
    Row(
        modifier = Modifier
            .horizontalScroll(scroll)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(QuizzDimensions.tvCodeCellGapDp.dp),
    ) {
        repeat(length) { index ->
            val active = enabled && index == value.length
            val stroke = when {
                isError -> colors.error
                active -> colors.primary
                else -> colors.borderVariant
            }
            Box(
                modifier = Modifier
                    .width(QuizzDimensions.tvCodeCellWidthDp.dp)
                    .height(QuizzDimensions.tvCodeCellHeightDp.dp)
                    .background(colors.surfaceVariant, MaterialTheme.shapes.medium)
                    .border(
                        BorderStroke(if (active) QuizzDimensions.tvFocusOutlineDp.dp else 1.dp, stroke),
                        MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value.getOrNull(index)?.toString().orEmpty(),
                    modifier = Modifier.clearAndSetSemantics { },
                    color = if (enabled) colors.onSurface else colors.onSurfaceVariant,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }
    }
}

@Composable
private fun CodeKeyboard(
    characters: String,
    enabled: Boolean,
    requestInitialFocus: Boolean,
    onEdit: (Char?) -> Unit,
) {
    val firstKey = remember { FocusRequester() }
    var initialFocusRequested by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(QuizzDimensions.tvCodeKeyboardPaddingDp.dp)
            .focusRestorer(firstKey)
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(QuizzDimensions.tvCodeKeyGapDp.dp),
    ) {
        characters.toList().distinct().forEachIndexed { index, character ->
            CodeKey(
                onClick = { onEdit(character) },
                enabled = enabled,
                modifier = if (index == 0) Modifier.focusRequester(firstKey) else Modifier,
            ) {
                Text(character.toString(), style = MaterialTheme.typography.headlineMedium)
            }
        }
        val deleteLabel = stringResource(R.string.quizz_tv_code_delete)
        CodeKey(
            onClick = { onEdit(null) },
            enabled = enabled,
            modifier = Modifier.semantics { contentDescription = deleteLabel },
        ) {
            CodeDeleteIcon()
        }
    }
    LaunchedEffect(enabled, requestInitialFocus) {
        if (enabled && requestInitialFocus && !initialFocusRequested) {
            initialFocusRequested = true
            firstKey.requestFocus()
        }
    }
}

@Composable
private fun CodeKey(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    val focusBorder = Border(BorderStroke(QuizzDimensions.tvFocusOutlineDp.dp, colors.primary), shape = shape)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .width(QuizzDimensions.tvCodeKeyWidthDp.dp)
            .height(QuizzDimensions.tvCodeKeyHeightDp.dp),
        shape = ButtonDefaults.shape(shape = shape),
        colors = ButtonDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = colors.onSurfaceVariant,
            focusedContainerColor = colors.primary,
            focusedContentColor = colors.onPrimary,
            pressedContainerColor = colors.primaryContainer,
            pressedContentColor = colors.onPrimaryContainer,
            disabledContainerColor = colors.surface,
            disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.38f),
        ),
        scale = ButtonDefaults.scale(focusedScale = QuizzDimensions.tvFocusScaleButton),
        border = ButtonDefaults.border(focusedBorder = focusBorder, focusedDisabledBorder = focusBorder),
        contentPadding = PaddingValues(0.dp),
        content = content,
    )
}

@Composable
private fun CodeDeleteIcon() {
    val color = LocalContentColor.current
    Canvas(Modifier.size(24.dp)) {
        val outline = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.5f)
            lineTo(size.width * 0.35f, size.height * 0.2f)
            lineTo(size.width * 0.9f, size.height * 0.2f)
            lineTo(size.width * 0.9f, size.height * 0.8f)
            lineTo(size.width * 0.35f, size.height * 0.8f)
            close()
        }
        val strokeWidth = 2.dp.toPx()
        drawPath(outline, color, style = Stroke(strokeWidth))
        drawLine(
            color,
            Offset(size.width * 0.47f, size.height * 0.35f),
            Offset(size.width * 0.73f, size.height * 0.65f),
            strokeWidth,
        )
        drawLine(
            color,
            Offset(size.width * 0.47f, size.height * 0.65f),
            Offset(size.width * 0.73f, size.height * 0.35f),
            strokeWidth,
        )
    }
}

private fun handleCodeHardwareKey(
    event: KeyEvent,
    characters: String,
    enabled: Boolean,
    onEdit: (Char?) -> Unit,
): Boolean {
    val native = event.nativeKeyEvent
    val unicode = native.unicodeChar
    val character = when {
        unicode in 1..Char.MAX_VALUE.code -> unicode.toChar()
        unicode == 0 -> hardwareDigit(native.keyCode)
        else -> null
    }?.takeIf { it in characters }
    val delete = native.keyCode == AndroidKeyEvent.KEYCODE_DEL ||
        native.keyCode == AndroidKeyEvent.KEYCODE_FORWARD_DEL
    if (character == null && !delete) return false
    if (enabled && native.action == AndroidKeyEvent.ACTION_DOWN && native.repeatCount == 0) {
        onEdit(if (delete) null else character)
    }
    return true
}

private fun hardwareDigit(keyCode: Int): Char? = when (keyCode) {
    in AndroidKeyEvent.KEYCODE_0..AndroidKeyEvent.KEYCODE_9 ->
        '0' + (keyCode - AndroidKeyEvent.KEYCODE_0)

    in AndroidKeyEvent.KEYCODE_NUMPAD_0..AndroidKeyEvent.KEYCODE_NUMPAD_9 ->
        '0' + (keyCode - AndroidKeyEvent.KEYCODE_NUMPAD_0)

    else -> null
}
