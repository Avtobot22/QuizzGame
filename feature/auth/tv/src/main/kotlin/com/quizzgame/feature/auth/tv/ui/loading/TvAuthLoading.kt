package com.quizzgame.feature.auth.tv.ui.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.designsystem.tv.QuizzTvSkeleton
import com.quizzgame.core.ui.QuizzGroupDivider
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.core.ui.tokens.QuizzTvLayout
import com.quizzgame.feature.auth.tv.R
import com.quizzgame.feature.auth.tv.ui.tags.TvSignInTags

@Composable
internal fun TvAuthMethodsLoading() {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(QuizzTvLayout.cornerRadius.dp)
    QuizzTvSkeleton(
        stringResource(R.string.auth_loading_methods),
        Modifier.fillMaxWidth().testTag(TvSignInTags.LOADING),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) { index ->
                if (index == 2) QuizzGroupDivider(colors.border.copy(alpha = 0.55f))
                Box(Modifier.fillMaxWidth().height(QuizzTvLayout.buttonHeight.dp).quizzSkeleton(shape))
            }
        }
    }
}
