package id.andreasmlbngaol.mpus.ui.sighting

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.ui.theme.ShapeCache
import id.andreasmlbngaol.mpus.ui.theme.SquircleShape
import id.andreasmlbngaol.mpus.util.CaptureFiles
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor
import kotlin.math.min

/**
 * A square-locked camera. The preview is shown inside a 1:1 box (so it is a centre-crop of
 * the sensor, exactly what gets kept) and the captured frame is centre-cropped to a square
 * before it leaves this screen. Nothing downstream ever sees a non-square photo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CameraCapture(
    onCaptured: (Uri) -> Unit,
    onFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor: Executor = remember { ContextCompat.getMainExecutor(context) }

    var capture by remember { mutableStateOf<ImageCapture?>(null) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    DisposableEffect(Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            runCatching {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                capture = imageCapture
            }.onFailure {
                failed = true
                onFailed()
            }
        }
        providerFuture.addListener(listener, executor)
        onDispose { runCatching { providerFuture.get().unbindAll() } }
    }

    // Once the preview is up, drop the failed flag so a transient provider hiccup that
    // recovered does not leave the error text stuck on screen.
    LaunchedEffect(capture) { if (capture != null) failed = false }

    Box(modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        // The viewfinder is a true 1:1 box, centred: the preview is a centre crop of the
        // sensor, so what you frame here is exactly what the capture keeps.
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(ShapeCache.smooth24),
        )

        FilledIconButton(
            onClick = onFailed,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp).size(48.dp),
            shape = SquircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.18f),
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.snap_close))
        }

        // Plain camera shutter: a white ring with a white disc, the shape every camera app
        // uses. No glyph.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.28f))
                .border(3.dp, Color.White, CircleShape)
                .clickable(enabled = capture != null && !busy) {
                    val ic = capture ?: return@clickable
                    if (busy) return@clickable
                    busy = true
                    val (tmpFile, _) = CaptureFiles.newImage(context)
                    ic.takePicture(
                        ImageCapture.OutputFileOptions.Builder(tmpFile).build(),
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val square = squareInto(tmpFile)
                                busy = false
                                if (square != null) onCaptured(square) else { failed = true; onFailed() }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                busy = false
                                failed = true
                                onFailed()
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }

        if (failed) {
            Text(
                stringResource(R.string.snap_err_no_camera),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
        }
    }
}

/**
 * Centre-crop the just-captured file to a square and write it back next to the original.
 * Returns the square file's Uri, or null if the capture could not be read.
 *
 * Uses ImageDecoder so the camera's EXIF rotation is applied: the raw file is stored
 * sideways and would otherwise come out rotated. Downsampled on decode so a 12 MP frame
 * never sits fully in memory.
 */
private fun squareInto(src: File): Uri? {
    return runCatching {
        val source = ImageDecoder.createSource(src)
        val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val w = info.size.width
            val h = info.size.height
            val longest = maxOf(w, h)
            if (longest > MaxSquareSource) {
                val ratio = MaxSquareSource.toFloat() / longest
                decoder.setTargetSize(
                    (w * ratio).toInt().coerceAtLeast(1),
                    (h * ratio).toInt().coerceAtLeast(1),
                )
            }
        }
        val side = min(decoded.width, decoded.height)
        val square = Bitmap.createBitmap(
            decoded,
            (decoded.width - side) / 2,
            (decoded.height - side) / 2,
            side,
            side,
        )
        val out = File(src.parentFile, "square_${src.name}")
        FileOutputStream(out).use { square.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        if (square !== decoded) square.recycle()
        decoded.recycle()
        src.delete()
        Uri.fromFile(out)
    }.getOrNull()
}

private const val MaxSquareSource = 1600
