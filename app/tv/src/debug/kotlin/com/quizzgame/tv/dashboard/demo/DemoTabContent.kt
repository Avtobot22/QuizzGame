package com.quizzgame.tv.dashboard.demo

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.tv.R

@Composable
internal fun DemoTabContent(
    tab: DashboardTabId,
    requestFocus: Boolean,
    open: (DashboardDestination) -> Unit,
) {
    var focusedItem by rememberSaveable { mutableIntStateOf(0) }
    LazyColumn(
        modifier = Modifier.testTag(DemoDashboardTags.tabContent(tab)).focusGroup(),
        state = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items((0 until 20).toList(), key = { it }) { id ->
            val focus = remember { FocusRequester() }
            QuizzTvButton(
                onClick = { open(DashboardDestination.QuizDetail("$tab-$id")) },
                modifier = Modifier.testTag(DemoDashboardTags.quiz(tab, id)).focusRequester(focus)
                    .onFocusChanged { if (it.isFocused) focusedItem = id },
            ) { Text(stringResource(R.string.demo_quiz, id + 1)) }
            LaunchedEffect(requestFocus) {
                if (requestFocus && focusedItem == id) focus.requestFocus()
            }
        }
    }
}

@Composable
internal fun DemoQuizDetail(quizId: String, back: () -> Unit, viewModel: DemoEntryViewModel = hiltViewModel()) {
    val focus = remember { FocusRequester() }
    Column(Modifier.testTag(DemoDashboardTags.DETAIL), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.demo_detail, quizId))
        Text(stringResource(R.string.demo_counter, viewModel.counter), Modifier.testTag(DemoDashboardTags.COUNTER))
        QuizzTvButton(viewModel::increment, Modifier.testTag(DemoDashboardTags.INCREMENT).focusRequester(focus)) {
            Text(stringResource(R.string.demo_increment))
        }
        QuizzTvButton(back, Modifier.testTag(DemoDashboardTags.BACK)) { Text(stringResource(R.string.demo_back)) }
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
}
