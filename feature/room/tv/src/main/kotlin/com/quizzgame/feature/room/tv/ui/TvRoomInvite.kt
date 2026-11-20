package com.quizzgame.feature.room.tv.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.quizzgame.feature.room.tv.R
import com.quizzgame.feature.room.tv.ui.tags.TvLobbyTags
import com.quizzgame.shared.room.domain.entity.snapshot.RoomInvite
import kotlin.math.floor

private const val LARGE_PIN_LENGTH = 6

@Composable
internal fun TvRoomInvite(invite: RoomInvite?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxWidth().fillMaxHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(colors.primaryContainer, colors.surfaceVariant)))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        TvRoomPin(invite?.code)
        val qrUrl = invite?.url
        if (qrUrl != null) {
            TvInviteDivider()
            TvQrInvite(qrUrl)
        } else if (invite != null) {
            Text(
                stringResource(R.string.room_pin_help),
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun TvRoomPin(code: String?) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            stringResource(R.string.room_pin),
            Modifier.semantics { heading() },
            color = colors.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
        )
        if (code == null) {
            Text(
                stringResource(R.string.room_invite_unavailable),
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            val typography = MaterialTheme.typography
            Text(
                code,
                Modifier.testTag(TvLobbyTags.CODE),
                color = colors.onPrimaryContainer,
                style = if (code.length <= LARGE_PIN_LENGTH) typography.displayLarge else typography.displaySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TvInviteDivider() {
    val colors = MaterialTheme.colorScheme
    val rule = Modifier.height(1.dp).background(colors.onPrimaryContainer.copy(alpha = 0.22f))
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(rule.weight(1f))
        Text(
            stringResource(R.string.room_invite_or),
            color = colors.onSurface,
            style = MaterialTheme.typography.labelMedium,
        )
        Box(rule.weight(1f))
    }
}

@Composable
private fun TvQrInvite(link: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RoomQr(link)
        Text(
            stringResource(R.string.room_invite_help),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RoomQr(link: String) {
    val matrix = remember(link) { QRCodeWriter().encode(link, BarcodeFormat.QR_CODE, 1, 1) }
    val description = stringResource(R.string.room_qr)
    Canvas(
        Modifier.size(164.dp).clip(MaterialTheme.shapes.medium)
            .background(Color.White).semantics { contentDescription = description },
    ) {
        val pixel = floor(size.width / matrix.width)
        val shift = floor((size.width - pixel * matrix.width) / 2)
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) {
                    drawRect(
                        Color.Black,
                        Offset(shift + x * pixel, shift + y * pixel),
                        Size(pixel, pixel),
                    )
                }
            }
        }
    }
}
