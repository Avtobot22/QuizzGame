package com.quizzgame.feature.room.tv.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.quizzgame.core.ui.QuizzIconLabel
import com.quizzgame.feature.room.tv.R
import com.quizzgame.feature.room.tv.ui.tags.TvLobbyTags
import com.quizzgame.shared.room.domain.entity.snapshot.RoomInvite
import kotlin.math.floor
import com.quizzgame.core.ui.R as UiR

private const val LARGE_PIN_LENGTH = 6

@Composable
internal fun TvRoomInvite(invite: RoomInvite?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxWidth().fillMaxHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.verticalGradient(
                    listOf(lerp(colors.surface, colors.primaryContainer, 0.76f), colors.surfaceVariant),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
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
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
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
                style = if (code.length <= LARGE_PIN_LENGTH) typography.displayLarge else typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TvInviteDivider() {
    Box(
        Modifier.fillMaxWidth(0.58f).height(1.dp)
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f)),
    )
}

@Composable
private fun TvQrInvite(link: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RoomQr(link)
        val color = MaterialTheme.colorScheme.onSurfaceVariant
        QuizzIconLabel(UiR.drawable.ic_quizz_scan, tint = color) {
            Text(
                stringResource(R.string.room_invite_help),
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
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
