package com.quizzgame.tv.dashboard.fixture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.quizzgame.core.designsystem.tv.QuizzTvButton
import com.quizzgame.core.navigation.focus.TvEntryFocus
import com.quizzgame.tv.dashboard.fixture.ui.tags.FixtureDashboardTags
import com.quizzgame.tv.navigation.fixtureString
import com.quizzgame.tv.test.R as FixtureR

@Composable
internal fun FixtureTabContent(
    tab: DashboardTabId,
    requestFocus: Boolean,
    open: (DashboardDestination) -> Unit,
) {
    val focus = remember { FocusRequester() }
    LazyColumn(
        modifier = Modifier.testTag(FixtureDashboardTags.tabContent(tab)).focusRequester(focus).focusRestorer(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items((0 until 20).toList(), key = { it }) { id ->
            QuizzTvButton(
                onClick = {
                    focus.saveFocusedChild()
                    open(DashboardDestination.QuizDetail("$tab-$id"))
                },
                modifier = Modifier.testTag(FixtureDashboardTags.quiz(tab, id)),
            ) { Text(fixtureString(FixtureR.string.fixture_quiz, id + 1)) }
        }
    }
    TvEntryFocus(focus, requestFocus)
}

@Composable
internal fun FixtureQuizDetail(
    quizId: String,
    back: () -> Unit,
    viewModel: FixtureEntryViewModel = viewModel { FixtureEntryViewModel() },
) {
    val focus = remember { FocusRequester() }
    Column(Modifier.testTag(FixtureDashboardTags.DETAIL), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(fixtureString(FixtureR.string.fixture_detail, quizId))
        Text(
            fixtureString(FixtureR.string.fixture_counter, viewModel.counter),
            Modifier.testTag(FixtureDashboardTags.COUNTER),
        )
        QuizzTvButton(
            viewModel::increment,
            Modifier.testTag(FixtureDashboardTags.INCREMENT)
                .focusRequester(focus),
        ) {
            Text(fixtureString(FixtureR.string.fixture_increment))
        }
        QuizzTvButton(
            back,
            Modifier.testTag(FixtureDashboardTags.BACK),
        ) { Text(fixtureString(FixtureR.string.fixture_back)) }
    }
    TvEntryFocus(focus)
}
