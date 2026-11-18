package com.quizzgame.feature.auth.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quizzgame.feature.auth.mobile.R

private val GOOGLE_TEXT = Color(0xFF1F1F1F)
private val GOOGLE_OUTLINE = Color(0xFF747775)

@Composable
internal fun GoogleAuthButton(modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick,
        modifier.fillMaxWidth().heightIn(min = 48.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = GOOGLE_TEXT),
        border = BorderStroke(1.dp, GOOGLE_OUTLINE),
    ) {
        Image(painterResource(R.drawable.auth_google_g), contentDescription = null, Modifier.size(18.dp))
        Spacer(Modifier.size(10.dp))
        Text(stringResource(R.string.auth_google))
    }
}
