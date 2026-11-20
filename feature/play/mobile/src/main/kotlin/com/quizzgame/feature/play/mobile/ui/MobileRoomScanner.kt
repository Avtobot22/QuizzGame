package com.quizzgame.feature.play.mobile.ui

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BinaryBitmap
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.quizzgame.core.designsystem.mobile.QuizzButton
import com.quizzgame.core.designsystem.mobile.QuizzButtonStyle
import com.quizzgame.feature.play.mobile.R
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
internal fun MobileRoomScanner(onDismiss: () -> Unit, onDecode: (String) -> Unit, onUnavailable: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val decode = rememberUpdatedState(onDecode)
    val unavailable = rememberUpdatedState(onUnavailable)
    val view = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            clipToOutline = true
        }
    }
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.padding(horizontal = 24.dp).widthIn(max = 480.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.play_scan), style = MaterialTheme.typography.titleLarge)
                AndroidView(
                    { view },
                    Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.large),
                )
                QuizzButton(onDismiss, style = QuizzButtonStyle.TEXT) { Text(stringResource(R.string.play_manual)) }
            }
        }
    }
    DisposableEffect(context, owner, view) {
        val camera = RoomCamera(context, owner, view, { decode.value(it) }, { unavailable.value() })
        onDispose { camera.close() }
    }
}

private class RoomCamera(
    context: Context,
    owner: LifecycleOwner,
    view: PreviewView,
    onDecode: (String) -> Unit,
    onUnavailable: () -> Unit,
) : Closeable {
    private val active = AtomicBoolean(true)
    private val delivered = AtomicBoolean()
    private val worker = Executors.newSingleThreadExecutor()
    private val analysis = ImageAnalysis.Builder().setBackpressureStrategy(
        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
    ).build()
    private val preview = Preview.Builder().build().apply { surfaceProvider = view.surfaceProvider }
    private var provider: ProcessCameraProvider? = null

    init {
        val main = ContextCompat.getMainExecutor(context)
        val reader = QRCodeReader()
        analysis.setAnalyzer(worker) { frame ->
            frame.use {
                if (active.get() && !delivered.get()) {
                    frame.readQr(reader)?.let { text ->
                        if (delivered.compareAndSet(false, true)) main.execute { if (active.get()) onDecode(text) }
                    }
                }
            }
        }
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (active.get()) {
                runCatching {
                    future.get().also {
                        provider = it
                        it.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                    }
                }.onFailure { onUnavailable() }
            }
        }, main)
    }

    override fun close() {
        active.set(false)
        analysis.clearAnalyzer()
        provider?.unbind(preview, analysis)
        worker.shutdown()
    }
}

private fun ImageProxy.readQr(reader: QRCodeReader): String? {
    val plane = planes.firstOrNull() ?: return null
    if (plane.pixelStride != 1) return null
    val padded = ByteArray(plane.rowStride * height)
    val buffer = plane.buffer
    buffer.get(padded, 0, minOf(buffer.remaining(), padded.size))
    val source = PlanarYUVLuminanceSource(padded, plane.rowStride, height, 0, 0, width, height, false)
    return try {
        reader.decode(BinaryBitmap(HybridBinarizer(source))).text
    } catch (_: ReaderException) {
        null
    }
}
