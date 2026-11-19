package com.quizzgame.feature.authprovider.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.quizzgame.core.designsystem.tv.QuizzTvSkeleton
import com.quizzgame.core.ui.quizzSkeleton
import com.quizzgame.feature.authprovider.tv.R

@Composable
internal fun TvDeviceAuthLoading() {
    QuizzTvSkeleton(stringResource(R.string.auth_device_loading), Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.fillMaxWidth(0.78f).height(22.dp).quizzSkeleton())
            Box(Modifier.size(TvAuthQrSize).quizzSkeleton(MaterialTheme.shapes.medium))
            Box(Modifier.fillMaxWidth(0.9f).height(20.dp).quizzSkeleton())
            Box(Modifier.fillMaxWidth(0.35f).height(32.dp).quizzSkeleton())
            Box(Modifier.fillMaxWidth(0.55f).height(16.dp).quizzSkeleton())
        }
    }
}
