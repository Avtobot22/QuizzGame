package com.quizzgame.feature.auth.mobile.ui.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileSkeleton
import com.quizzgame.core.ui.QuizzGroupDivider
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.feature.auth.mobile.R
import com.quizzgame.feature.auth.mobile.ui.tags.MobileSignInTags

@Composable
internal fun MobileAuthLoading() {
    val shape = MaterialTheme.shapes.large
    QuizzMobileSkeleton(
        stringResource(R.string.auth_loading_methods),
        Modifier.fillMaxWidth().testTag(MobileSignInTags.LOADING),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                if (index == 2) QuizzGroupDivider(MaterialTheme.colorScheme.outlineVariant)
                Box(Modifier.fillMaxWidth().height(52.dp).quizzSkeleton(shape))
            }
        }
    }
}
