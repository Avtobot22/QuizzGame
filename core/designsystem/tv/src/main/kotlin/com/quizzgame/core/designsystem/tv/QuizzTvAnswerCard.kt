package com.quizzgame.core.designsystem.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.SelectableSurfaceColors
import androidx.tv.material3.SelectableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.quizzgame.core.ui.LocalQuizzGameColors
import com.quizzgame.core.ui.QuizzGameColors
import com.quizzgame.core.ui.R
import com.quizzgame.core.ui.tokens.QuizzDimensions

@Composable
fun QuizzTvAnswerCard(
    title: String,
    helperText: String,
    state: AnswerVisualState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    val game = LocalQuizzGameColors.current
    val background = game.backgroundFor(state)
    val foreground = game.contentFor(state)
    val feedback = when (state) {
        AnswerVisualState.CORRECT -> stringResource(R.string.quizz_answer_correct)
        AnswerVisualState.WRONG -> stringResource(R.string.quizz_answer_wrong)
        else -> null
    }
    val shape = RoundedCornerShape(27.dp)
    val focusBorder = Border(
        BorderStroke(QuizzDimensions.tvFocusOutlineDp.dp, MaterialTheme.colorScheme.border),
        shape = shape,
    )
    Surface(
        selected = state == AnswerVisualState.SELECTED,
        onClick = onClick,
        enabled = state != AnswerVisualState.DISABLED,
        modifier = modifier.semantics { if (feedback != null) stateDescription = feedback },
        interactionSource = interactionSource,
        shape = SelectableSurfaceDefaults.shape(shape = shape),
        colors = answerSurfaceColors(background, foreground),
        scale = SelectableSurfaceDefaults.scale(
            focusedScale = QuizzDimensions.tvFocusScaleCard,
            focusedSelectedScale = QuizzDimensions.tvFocusScaleCard,
            focusedDisabledScale = QuizzDimensions.tvFocusScaleCard,
        ),
        border = SelectableSurfaceDefaults.border(
            focusedBorder = focusBorder,
            focusedDisabledBorder = focusBorder,
        ),
    ) {
        AnswerCardContent(title, helperText, state)
    }
}

@Composable
private fun answerSurfaceColors(background: Color, foreground: Color): SelectableSurfaceColors =
    SelectableSurfaceDefaults.colors(
        containerColor = background,
        contentColor = foreground,
        focusedContainerColor = background,
        focusedContentColor = foreground,
        pressedContainerColor = background,
        pressedContentColor = foreground,
        selectedContainerColor = background,
        selectedContentColor = foreground,
        focusedSelectedContainerColor = background,
        focusedSelectedContentColor = foreground,
        pressedSelectedContainerColor = background,
        pressedSelectedContentColor = foreground,
        disabledContainerColor = background,
        disabledContentColor = foreground,
    )

private fun QuizzGameColors.backgroundFor(state: AnswerVisualState) = when (state) {
    AnswerVisualState.CORRECT -> answerCorrect
    AnswerVisualState.WRONG -> answerWrong
    AnswerVisualState.SELECTED -> answerSelected
    AnswerVisualState.IDLE, AnswerVisualState.DISABLED -> answerIdle
}

private fun QuizzGameColors.contentFor(state: AnswerVisualState) = when (state) {
    AnswerVisualState.CORRECT -> onAnswerCorrect
    AnswerVisualState.WRONG -> onAnswerWrong
    AnswerVisualState.SELECTED -> onAnswerSelected
    AnswerVisualState.IDLE, AnswerVisualState.DISABLED -> onAnswerIdle
}

@Composable
private fun AnswerCardContent(title: String, helperText: String, state: AnswerVisualState) {
    Column(
        Modifier.padding(start = 34.dp, end = 30.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = TvComponentType.answerTitle)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state == AnswerVisualState.CORRECT || state == AnswerVisualState.WRONG) {
                Text(
                    if (state == AnswerVisualState.CORRECT) "✓" else "✕",
                    modifier = Modifier.clearAndSetSemantics {},
                    style = TvComponentType.answerHelper,
                )
            }
            Text(helperText, style = TvComponentType.answerHelper)
        }
    }
}
