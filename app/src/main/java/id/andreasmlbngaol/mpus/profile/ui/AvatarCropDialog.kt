package id.andreasmlbngaol.mpus.profile.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.ui.theme.ShapeCache
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The bare-minimum 1:1 cropper: a square viewport, pinch to zoom, drag to move, and whatever
 * is inside the square is what gets uploaded. No fancy grid, no rotation — you pick the area.
 */
@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun AvatarCropDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onCropped: (ByteArray) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var viewport by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) { decode(context, uri) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Size the square off whatever space is left after the title/hint/buttons, not off
        // the window width. On a short, wide screen (foldable, landscape) a width-driven
        // square grows taller than the screen and pushes the confirm button out of reach.
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            // maxWidth/maxHeight here are the content constraints (the Box's own padding is
            // already excluded), so the square can be the full remaining width — but never
            // taller than what's left after the title/hint/buttons (~240 dp).
            val side = minOf(maxWidth, maxHeight - 240.dp).coerceAtLeast(120.dp)
            Surface(
                shape = ShapeCache.smooth28,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.profile_crop_title),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.profile_crop_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Box(
                        modifier = Modifier
                            .size(side)
                            .clip(ShapeCache.smooth24)
                            .background(Color.Black)
                            .onSizeChanged { viewport = it.width.toFloat() }
                            .pointerInput(bitmap, viewport) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    val bmp = bitmap ?: return@detectTransformGestures
                                    val s = viewport
                                    if (s <= 0f) return@detectTransformGestures
                                    val base = maxOf(s / bmp.width, s / bmp.height)
                                    zoom = (zoom * gestureZoom).coerceIn(1f, 5f)
                                    offset = clampOffset(
                                        offset + pan,
                                        bmp.width * base * zoom,
                                        bmp.height * base * zoom,
                                        s,
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        val bmp = bitmap
                        if (bmp != null && viewport > 0f) {
                            val base = maxOf(viewport / bmp.width, viewport / bmp.height)
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(
                                        with(density) { (bmp.width * base).toDp() },
                                        with(density) { (bmp.height * base).toDp() },
                                    )
                                    .graphicsLayer {
                                        scaleX = zoom
                                        scaleY = zoom
                                        translationX = offset.x
                                        translationY = offset.y
                                    },
                            )
                        } else {
                            ContainedLoadingIndicator()
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.action_cancel))
                        }
                        Button(
                            onClick = {
                                val bmp = bitmap
                                if (bmp != null && viewport > 0f) {
                                    scope.launch {
                                        val bytes = withContext(Dispatchers.IO) {
                                            cropSquare(bmp, viewport, zoom, offset)
                                        }
                                        onCropped(bytes)
                                        onDismiss()
                                    }
                                } else {
                                    onDismiss()
                                }
                            },
                            enabled = bitmap != null,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = ShapeCache.smooth18,
                        ) { Text(stringResource(R.string.profile_crop_confirm)) }
                    }
                }
            }
        }
    }
}

/** Keep the image covering the square: it can never be dragged far enough to reveal a gap. */
private fun clampOffset(o: Offset, displayedW: Float, displayedH: Float, side: Float): Offset {
    val mx = ((displayedW - side) / 2f).coerceAtLeast(0f)
    val my = ((displayedH - side) / 2f).coerceAtLeast(0f)
    return Offset(o.x.coerceIn(-mx, mx), o.y.coerceIn(-my, my))
}

/** Whatever sits inside the square viewport, pulled out of the source at 512x512. */
private fun cropSquare(bmp: Bitmap, side: Float, zoom: Float, offset: Offset): ByteArray {
    val base = maxOf(side / bmp.width, side / bmp.height)
    val eff = base * zoom
    val cropSide = (side / eff).coerceAtLeast(1f)
    val cx = bmp.width / 2f - offset.x / eff
    val cy = bmp.height / 2f - offset.y / eff
    val size = cropSide.toInt().coerceAtLeast(1).coerceAtMost(minOf(bmp.width, bmp.height))
    val left = (cx - cropSide / 2f).toInt().coerceIn(0, bmp.width - size)
    val top = (cy - cropSide / 2f).toInt().coerceIn(0, bmp.height - size)
    val square = Bitmap.createBitmap(bmp, left, top, size, size)
    val scaled = Bitmap.createScaledBitmap(square, 512, 512, true)
    return ByteArrayOutputStream().use { out ->
        @Suppress("DEPRECATION")
        scaled.compress(Bitmap.CompressFormat.WEBP, 90, out)
        out.toByteArray()
    }
}

/** ImageDecoder applies EXIF orientation for us and lets us downsample before we hold pixels. */
private fun decode(context: Context, uri: Uri): Bitmap? = runCatching {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val w = info.size.width
        val h = info.size.height
        val longest = maxOf(w, h)
        if (longest > MaxDim) {
            val ratio = MaxDim.toFloat() / longest
            decoder.setTargetSize(
                (w * ratio).toInt().coerceAtLeast(1),
                (h * ratio).toInt().coerceAtLeast(1),
            )
        }
    }
}.getOrNull()

private const val MaxDim = 1600
