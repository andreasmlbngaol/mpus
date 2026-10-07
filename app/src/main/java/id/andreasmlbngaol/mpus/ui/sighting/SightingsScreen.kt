package id.andreasmlbngaol.mpus.ui.sighting

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.Candidate
import id.andreasmlbngaol.mpus.ui.asString
import id.andreasmlbngaol.mpus.ui.components.ExpressiveField
import id.andreasmlbngaol.mpus.ui.components.MpusTopBar
import id.andreasmlbngaol.mpus.ui.theme.ShapeCache
import id.andreasmlbngaol.mpus.ui.theme.SquircleShape
import id.andreasmlbngaol.mpus.ui.resolve
import id.andreasmlbngaol.mpus.util.CaptureFiles

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SightingsScreen(vm: SightingsViewModel, onOpenCat: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var hasCamera by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    LaunchedEffect(Unit) { vm.toast.collect { snackbar.showSnackbar(it.resolve(context)) } }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { ok -> if (ok) pendingUri?.let(vm::onPhotoCaptured) }

    val takePhoto: () -> Unit = {
        val (_, uri) = CaptureFiles.newImage(context)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }

    // Camera permission gate. Requested the first time this screen is shown; once granted
    // the camera opens straight away. A denial just leaves the empty state up so the user
    // can grant it from the button, rather than a dead screen.
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCamera = granted
        if (granted) takePhoto()
        else vm.onCameraDenied()
    }

    val openCamera: () -> Unit = {
        if (hasCamera) takePhoto() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Tapping the Snap tab *is* the action — ask for the camera straight away instead of
    // showing a page whose only job is to hold one button. The hero below stays as the
    // recovery state for when the camera is cancelled or unavailable.
    LaunchedEffect(Unit) {
        if (state.photoUri == null) openCamera()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MpusTopBar(
                title = stringResource(R.string.snap_title),
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            if (state.photoUri == null) {
                EmptyCapture(openCamera)
            } else {
                CaptureFlow(
                    state = state,
                    onRetake = openCamera,
                    onUpload = vm::upload,
                    onLink = vm::linkTo,
                    onName = vm::nameNew,
                    onOpenCat = onOpenCat,
                    onReset = vm::reset,
                )
            }
        }
    }
}

@Composable
private fun EmptyCapture(onTake: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Empty-state badge: tinted circle + oversized icon, the same shape language as
        // the retry state.
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.AddCircle,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.snap_found),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.snap_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        // Hero CTA: the one big, obvious action on this screen. Taller than the body
        // buttons (56dp vs 48dp) and rounded to a squircle, matching PixelPlayer's
        // MediumExtendedFloatingActionButton treatment for the primary action.
        Button(
            onClick = onTake,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = SquircleShape,
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.snap_open_camera),
                style = MaterialTheme.typography.titleMediumEmphasized,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CaptureFlow(
    state: SightingUiState,
    onRetake: () -> Unit,
    onUpload: () -> Unit,
    onLink: (String) -> Unit,
    onName: (String) -> Unit,
    onOpenCat: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        AsyncImage(
            model = state.photoUri,
            contentDescription = stringResource(R.string.cd_your_cat_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        )

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                when {
                    state.locating -> stringResource(R.string.snap_locating)
                    state.lat != null -> stringResource(R.string.snap_located)
                    else -> stringResource(R.string.snap_no_location)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onRetake) { Text(stringResource(R.string.snap_retake)) }
        }

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))

        val resolvedCatId = state.resolvedCatId
        when {
            resolvedCatId != null -> ResolvedCard(onOpenCat = { onOpenCat(resolvedCatId) }, onReset = onReset)
            state.result != null -> IdentifyResult(state, onLink, onName, onOpenCat, onReset)
            else -> {
                Button(
                    onClick = onUpload,
                    enabled = !state.busy && !state.locating,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = SquircleShape,
                ) {
                    if (state.busy) {
                        LoadingIndicator(Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.snap_identify), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun IdentifyResult(
    state: SightingUiState,
    onLink: (String) -> Unit,
    onName: (String) -> Unit,
    onOpenCat: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val candidates = state.result?.candidates.orEmpty()
    var newName by remember { mutableStateOf("") }

    Column(modifier) {
        Text(
            stringResource(R.string.snap_who),
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        Spacer(Modifier.height(4.dp))

        if (candidates.isEmpty()) {
            Text(
                stringResource(R.string.snap_no_matches),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                stringResource(R.string.snap_possible),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            candidates.forEach { c -> CandidateCard(c, onLink, onOpenCat) }
            Spacer(Modifier.height(20.dp))
        }

        Text(
            stringResource(R.string.snap_new_cat),
            style = MaterialTheme.typography.titleMediumEmphasized,
        )
        Spacer(Modifier.height(8.dp))
        ExpressiveField(
            value = newName,
            onValueChange = { newName = it },
            label = stringResource(R.string.snap_name_label),
            leadingIcon = Icons.Filled.Edit,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onName(newName) },
            enabled = newName.isNotBlank() && !state.busy,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = SquircleShape,
        ) { Text(stringResource(R.string.snap_create), style = MaterialTheme.typography.labelLarge) }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_done))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CandidateCard(
    c: Candidate,
    onLink: (String) -> Unit,
    onOpenCat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pct = (c.similarity * 100).toInt()
    val sameCat = stringResource(R.string.snap_same_cat)
    val view = stringResource(R.string.snap_view)
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = c.thumbUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.displayName ?: stringResource(R.string.cat_unnamed), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    LinearWavyProgressIndicator(
                        progress = { c.similarity.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.snap_similar, pct),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // Expressive button group: related actions on one compact, animated row.
            ButtonGroup(
                overflowIndicator = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                clickableItem(onClick = { onLink(c.catId) }, label = sameCat)
                clickableItem(onClick = { onOpenCat(c.catId) }, label = view)
            }
        }
    }
}

@Composable
private fun ResolvedCard(onOpenCat: () -> Unit, onReset: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.snap_saved_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.snap_saved_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenCat,
                    modifier = Modifier.height(48.dp),
                    shape = SquircleShape,
                ) { Text(stringResource(R.string.snap_open_cat)) }
                TextButton(onClick = onReset) { Text(stringResource(R.string.snap_another)) }
            }
        }
    }
}
