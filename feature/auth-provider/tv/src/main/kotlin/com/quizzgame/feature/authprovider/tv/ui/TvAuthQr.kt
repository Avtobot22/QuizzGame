package com.quizzgame.feature.authprovider.tv.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.quizzgame.feature.authprovider.tv.R

@Composable
internal fun TvAuthQr(uri: String, modifier: Modifier = Modifier) {
    val image = remember(uri) {
        val matrix = QRCodeWriter().encode(uri, BarcodeFormat.QR_CODE, RASTER_SIZE, RASTER_SIZE)
        val pixels = IntArray(matrix.width * matrix.height) { index ->
            if (matrix[index % matrix.width, index / matrix.width]) PIXEL_BLACK else PIXEL_WHITE
        }
        Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    Image(
        BitmapPainter(image, filterQuality = FilterQuality.None),
        contentDescription = stringResource(R.string.auth_device_qr),
        modifier = modifier,
    )
}

internal val TvAuthQrSize = 132.dp

private const val RASTER_SIZE = 256
private const val PIXEL_BLACK = 0xFF000000.toInt()
private const val PIXEL_WHITE = 0xFFFFFFFF.toInt()
