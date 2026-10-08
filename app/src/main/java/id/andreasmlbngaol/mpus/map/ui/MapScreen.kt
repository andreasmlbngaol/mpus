package id.andreasmlbngaol.mpus.map.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.core.ui.asString
import id.andreasmlbngaol.mpus.core.ui.resolve
import id.andreasmlbngaol.mpus.core.ui.components.ExpressiveState
import id.andreasmlbngaol.mpus.core.ui.components.MpusTopBar
import id.andreasmlbngaol.mpus.core.ui.theme.SquircleShape
import id.andreasmlbngaol.mpus.map.domain.model.Bounds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.ceil
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapScreen(vm: MapViewModel, onOpenCat: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary.toArgb()
    val mapBackground = MaterialTheme.colorScheme.surface.toArgb()
    val unnamed = stringResource(R.string.map_unnamed_marker)

    val mapView = remember {
        // Tiles download two-at-a-time by default, which is why the first view takes a
        // moment to fill in. Six parallel fetches plus a bigger queue makes the initial
        // draw noticeably snappier; osmdroid still respects the tile server's limits.
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            tileDownloadThreads = 6
            tileDownloadMaxQueueSize = 80
        }
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(16.0)
            controller.setCenter(GeoPoint(-6.2, 106.816))
        }
    }

    // While tiles are still downloading, osmdroid paints a grey grid placeholder. That grid
    // peeks out behind the translucent floating nav bar during a fling and reads as a
    // torn/unrendered edge. Fill it with the app surface instead and drop the grid lines so
    // the unloaded area blends in. (setBackgroundColor feeds the TilesOverlay loading tile.)
    LaunchedEffect(mapBackground) {
        mapView.setBackgroundColor(mapBackground)
        mapView.mapOverlay.setLoadingLineColor(android.graphics.Color.TRANSPARENT)
    }

    val toast = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    LaunchedEffect(Unit) { vm.onEvent(MapUiEvent.Started) }

    // Effects: center the camera on a fix, or surface a message.
    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is MapUiEffect.CenterOn -> {
                    mapView.controller.animateTo(GeoPoint(effect.point.lat, effect.point.lng))
                    mapView.controller.setZoom(16.0)
                }
                is MapUiEffect.ShowMessage -> toast.showSnackbar(effect.text.resolve(context))
            }
        }
    }

    // The factory sets the camera before the view is laid out, so the first bounding box
    // does not exist yet and no MapListener event fires. Report once after layout so the
    // first load doesn't wait for the user to nudge the map.
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        reportViewport(mapView) { bounds, zoom -> vm.onEvent(MapUiEvent.ViewportChanged(bounds, zoom)) }
    }

    // Center on the user's own fix the first time it lands.
    val myLocation = state.myLocation
    LaunchedEffect(myLocation) {
        myLocation?.let { mapView.controller.animateTo(GeoPoint(it.lat, it.lng)) }
    }

    // Redraw markers whenever the viewport result changes. Each pin shows the cat's face
    // (its thumbnail) inside a coloured ring with the name on a pill below — so a cat is
    // recognisable on the map without tapping through.
    LaunchedEffect(state.markers, accent, unnamed, myLocation) {
        val markers = state.markers
        val loader = SingletonImageLoader.get(context)
        val faces = coroutineScope {
            markers.map { cat ->
                async {
                    runCatching {
                        val request = ImageRequest.Builder(context)
                            .data(cat.thumbUrl)
                            .size(FaceSizePx)
                            .allowHardware(false)
                            .build()
                        (loader.execute(request) as? SuccessResult)?.image?.toBitmap()
                    }.getOrNull()
                }
            }.awaitAll()
        }

        mapView.overlays.clear()
        // The "you are here" dot goes under the cat pins so a cat sitting on top of the
        // user is still tappable.
        myLocation?.let { here ->
            mapView.overlays.add(
                Marker(mapView).apply {
                    position = GeoPoint(here.lat, here.lng)
                    icon = myLocationDot(context)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ -> false }
                },
            )
        }
        markers.forEachIndexed { i, cat ->
            val label = cat.displayName ?: unnamed
            val marker = Marker(mapView).apply {
                position = GeoPoint(cat.lat, cat.lng)
                title = label
                snippet = context.resources.getQuantityString(
                    R.plurals.sighting_count,
                    cat.sightingCount.toInt(),
                    cat.sightingCount,
                )
                icon = catPin(context, faces[i], label, accent)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    vm.onEvent(MapUiEvent.Select(cat))
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    // Report viewport changes (debounced in the VM).
    DisposableEffect(mapView) {
        val listener = object : org.osmdroid.events.MapListener {
            override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                reportViewport(mapView) { bounds, zoom -> vm.onEvent(MapUiEvent.ViewportChanged(bounds, zoom)) }
                return false
            }

            override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                reportViewport(mapView) { bounds, zoom -> vm.onEvent(MapUiEvent.ViewportChanged(bounds, zoom)) }
                return false
            }
        }
        mapView.addMapListener(listener)
        onDispose { mapView.removeMapListener(listener) }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Same collapsible bar as every other tab. The map has no scroll source, so it
        // stays in its expanded state — but it is the identical component, not a second
        // small bar, which is what made the tabs look like different apps.
        MpusTopBar(
            title = stringResource(R.string.map_title),
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (state.loading) {
            ContainedLoadingIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        }

        state.error?.let {
            ExpressiveState(
                message = stringResource(R.string.map_err_load),
                description = it.asString(),
                onRetry = { vm.onEvent(MapUiEvent.Refresh) },
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            )
        }

        // Expressive floating toolbar for quick map actions. The outer Scaffold already
        // reserves the navigation-bar space, so only a small margin is needed here.
        HorizontalFloatingToolbar(
            expanded = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        ) {
            FilledIconButton(onClick = { vm.onEvent(MapUiEvent.Refresh) }) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.map_refresh))
            }
            Spacer(Modifier.width(4.dp))
            FilledIconButton(onClick = { vm.onEvent(MapUiEvent.Recenter) }) {
                Icon(Icons.Filled.Place, contentDescription = stringResource(R.string.map_recenter))
            }
        }

        SnackbarHost(toast, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))
    }

    val selected = state.selected
    if (selected != null) {
        val sheetState = rememberBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
        ModalBottomSheet(onDismissRequest = { vm.onEvent(MapUiEvent.Select(null)) }, sheetState = sheetState) {
            CatSheet(
                cat = selected,
                onOpenCat = {
                    // Dismiss before navigating: a sheet left open behind the cat page keeps
                    // a back press of its own, which is what made back feel stuck.
                    vm.onEvent(MapUiEvent.Select(null))
                    onOpenCat(selected.id)
                },
            )
        }
    }
}

@Composable
private fun CatSheet(cat: CatMarker, onOpenCat: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = cat.thumbUrl,
                contentDescription = cat.displayName,
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    cat.displayName ?: stringResource(R.string.map_unnamed_cat),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                )
                Text(
                    pluralStringResource(
                        R.plurals.sighting_count_around,
                        cat.sightingCount.toInt(),
                        cat.sightingCount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onOpenCat(cat.id) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = SquircleShape,
        ) { Text(stringResource(R.string.map_open_cat)) }
    }
}

/** Marker bitmap size in px for the loaded face thumbnail. */
private const val FaceSizePx = 256

/**
 * A classic "you are here" dot: a soft blue halo, a white ring, and a solid blue core,
 * centred on the user's fix.
 */
private fun myLocationDot(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val haloD = 24f * density
    val coreD = 14f * density
    val size = ceil(haloD).toInt()
    val bmp = createBitmap(size, size)
    val c = Canvas(bmp)
    val cx = size / 2f
    val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x332196F3 }
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    val core = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2196F3.toInt() }
    c.drawCircle(cx, cx, haloD / 2f, halo)
    c.drawCircle(cx, cx, coreD / 2f + 3f * density, ring)
    c.drawCircle(cx, cx, coreD / 2f, core)
    return BitmapDrawable(context.resources, bmp)
}

/**
 * Builds a map pin from a cat's face: a circle clipped to the thumbnail (or a paw-ish
 * placeholder when the image failed), a coloured ring, a small pointer, and the cat's
 * name on a rounded pill below. Drawn once per marker into a bitmap, so the map just
 * blits it on every frame.
 */
private fun catPin(context: Context, face: Bitmap?, name: String, accent: Int): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val faceD = 46f * density
    val ringD = 3.5f * density
    val gapD = 3f * density
    val padD = 4f * density
    val pointerD = 8f * density
    val pillPadHD = 8f * density
    val pillPadVD = 4f * density
    val labelTextSize = 15f * density

    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1B1B1B.toInt()
        textSize = labelTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    // A plain bold face keeps this bitmap helper free of Compose/Resource plumbing and
    // reads fine at 15sp; names longer than a few characters are ellipsised.
    val label = TextUtils.ellipsize(name, textPaint, faceD * 3.5f, TextUtils.TruncateAt.END).toString()
    val textW = textPaint.measureText(label)
    val fontMetrics = textPaint.fontMetrics
    val textH = fontMetrics.descent - fontMetrics.ascent

    val contentW = maxOf(faceD + ringD * 2, textW + pillPadHD * 2)
    val width = (contentW + padD * 2).toInt()
    val height = (padD * 2 + faceD + ringD * 2 + gapD + textH + pillPadVD * 2 + pointerD).toInt()
    val bmp = createBitmap(width, height)
    val c = Canvas(bmp)

    val ringColor = accent
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ringColor; style = Paint.Style.FILL }
    val ringWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
    val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
    val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ringColor; style = Paint.Style.FILL }

    val cx = width / 2f
    val ringR = faceD / 2f + ringD
    val faceCy = padD + ringR

    c.drawCircle(cx, faceCy, ringR, ringPaint)
    c.drawCircle(cx, faceCy, ringR - ringD * 0.5f, ringWhite)

    val faceR = faceD / 2f
    if (face != null) {
        c.save()
        c.clipPath(
            android.graphics.Path().apply { addCircle(cx, faceCy, faceR, android.graphics.Path.Direction.CW) },
        )
        // Center-crop the square thumbnail into the face circle.
        val src = Rect(0, 0, face.width, face.height)
        val dst = RectF(cx - faceR, faceCy - faceR, cx + faceR, faceCy + faceR)
        c.drawBitmap(face, src, dst, Paint(Paint.FILTER_BITMAP_FLAG))
        c.restore()
    } else {
        c.drawCircle(cx, faceCy, faceR, ringWhite)
        c.drawCircle(cx, faceCy, faceR * 0.35f, ringPaint)
    }

    // Name pill below the face.
    val pillTop = faceCy + ringR + gapD
    val pillLeft = cx - contentW / 2f
    val pillRect = RectF(pillLeft, pillTop, cx + contentW / 2f, pillTop + textH + pillPadVD * 2)
    val pillRadius = pillRect.height() / 2f
    c.drawRoundRect(pillRect, pillRadius, pillRadius, pillPaint)

    // Pointer from the pill's bottom edge down to the anchor tip.
    val pointerPath = android.graphics.Path().apply {
        moveTo(cx, height.toFloat())
        lineTo(cx - pointerD, pillRect.bottom)
        lineTo(cx + pointerD, pillRect.bottom)
        close()
    }
    c.drawPath(pointerPath, pointerPaint)

    val baseline = pillRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
    c.drawText(label, cx, baseline, textPaint)

    return BitmapDrawable(context.resources, bmp)
}

/** Pulls the current bbox and zoom off the map and hands them to [onViewport]. */
private fun reportViewport(mapView: MapView, onViewport: (Bounds, Double) -> Unit) {
    val bb = mapView.boundingBox ?: return
    onViewport(
        Bounds(
            minLat = bb.latSouth,
            minLng = bb.lonWest,
            maxLat = bb.latNorth,
            maxLng = bb.lonEast,
        ),
        mapView.zoomLevelDouble,
    )
}
