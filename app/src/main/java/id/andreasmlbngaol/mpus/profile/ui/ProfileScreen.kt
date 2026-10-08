package id.andreasmlbngaol.mpus.profile.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.domain.model.Sighting
import id.andreasmlbngaol.mpus.core.domain.model.User
import id.andreasmlbngaol.mpus.core.ui.asString
import id.andreasmlbngaol.mpus.core.ui.components.ExpressiveState
import id.andreasmlbngaol.mpus.core.ui.components.MpusTopBar
import id.andreasmlbngaol.mpus.core.ui.resolve
import id.andreasmlbngaol.mpus.core.ui.theme.ShapeCache
import id.andreasmlbngaol.mpus.core.ui.util.CaptureFiles
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    user: User?,
    onOpenCat: (String) -> Unit,
    onOpenNotifications: () -> Unit = {},
    onEditProfile: () -> Unit = {},
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is ProfileUiEffect.ShowMessage -> snackbar.showSnackbar(effect.text.resolve(context))
            }
        }
    }

    // Keep the bell badge fresh while the tab is on screen; stop when it leaves.
    LaunchedEffect(Unit) {
        while (true) {
            vm.onEvent(ProfileUiEvent.RefreshUnread)
            delay(30_000)
        }
    }

    // Tapping the avatar opens an action sheet (view / upload / camera / delete) rather
    // than jumping straight into the picker, so "just look at my photo" is possible too.
    var sheetOpen by remember { mutableStateOf(false) }
    var viewingAvatar by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    // A picked/captured image lands here and goes through the 1:1 cropper before it uploads.
    var cropUri by remember { mutableStateOf<Uri?>(null) }

    val upload: (Uri?) -> Unit = { uri -> if (uri != null) cropUri = uri }

    val avatarPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { upload(it) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { ok -> if (ok) upload(pendingCameraUri) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) {
        val (_, uri) = CaptureFiles.newImage(context)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    } }

    val openCamera: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val (_, uri) = CaptureFiles.newImage(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MpusTopBar(
                title = stringResource(R.string.profile_title),
                scrollBehavior = scrollBehavior,
                actions = {
                    NotificationsBell(unread = state.unread, onClick = onOpenNotifications)
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            // Identity card: squircle surfaceContainerHigh with a circular avatar, the
            // PixelPlayer profile-header pattern.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = ShapeCache.smooth20,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .clickable { sheetOpen = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        val url = user?.avatarUrl
                        if (url != null) {
                            AsyncImage(
                                model = url,
                                contentDescription = stringResource(R.string.cd_change_avatar),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            user?.nickname ?: stringResource(R.string.profile_placeholder),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(
                                R.string.profile_handle,
                                user?.username ?: stringResource(R.string.profile_placeholder),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onEditProfile,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = ShapeCache.smooth18,
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_edit), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { vm.onEvent(ProfileUiEvent.Logout) },
                    modifier = Modifier.height(48.dp),
                    shape = ShapeCache.smooth18,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_sign_out), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
            // Stats give the page a body of its own instead of a lone grid. These are the
            // server's exact totals, not the size of the (paged) grid below.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileStatTile(
                    label = stringResource(R.string.profile_stat_sightings),
                    value = state.sightingsCount.toString(),
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                ProfileStatTile(
                    label = stringResource(R.string.profile_stat_cats),
                    value = state.catsCount.toString(),
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                ProfileStatTile(
                    label = stringResource(R.string.profile_stat_unnamed),
                    value = state.unnamedCount.toString(),
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    content = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.profile_your_sightings),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    modifier = Modifier.weight(1f),
                )
                if (state.sightingsCount > 0) {
                    Text(
                        state.sightingsCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            val error = state.error
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ContainedLoadingIndicator()
                }
                error != null -> ExpressiveState(
                    message = stringResource(R.string.profile_err_load),
                    description = error.asString(),
                    onRetry = { vm.onEvent(ProfileUiEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                )
                state.sightings.isEmpty() -> EmptySightings(Modifier.fillMaxSize())
                else -> {
                    // Pull the next page a screenful before the end, so scrolling never
                    // hits a dead stop. `totalItemsCount` comes from the grid itself, so
                    // this reads live layout state instead of a stale list size.
                    val gridState = rememberLazyGridState()
                    val nearEnd by remember {
                        derivedStateOf {
                            val layout = gridState.layoutInfo
                            val last = layout.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
                            layout.totalItemsCount > 0 && last >= layout.totalItemsCount - 6
                        }
                    }
                    LaunchedEffect(nearEnd, state.nextCursor) {
                        if (nearEnd) vm.onEvent(ProfileUiEvent.LoadMore)
                    }
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.sightings, key = { it.id }) { s -> SightingTile(s, onOpenCat) }
                        if (state.loadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    ContainedLoadingIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (sheetOpen) {
        AvatarActionSheet(
            hasPhoto = user?.avatarUrl != null,
            onDismiss = { sheetOpen = false },
            onView = {
                sheetOpen = false
                viewingAvatar = user?.avatarUrl
            },
            onUpload = {
                sheetOpen = false
                avatarPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onCamera = {
                sheetOpen = false
                openCamera()
            },
            onDelete = {
                sheetOpen = false
                vm.onEvent(ProfileUiEvent.DeleteAvatar)
            },
        )
    }

    viewingAvatar?.let { url ->
        Dialog(onDismissRequest = { viewingAvatar = null }) {
            AsyncImage(
                model = url,
                contentDescription = stringResource(R.string.cd_profile_photo),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeCache.smooth24)
                    .clickable { viewingAvatar = null },
            )
        }
    }

    cropUri?.let { uri ->
        AvatarCropDialog(
            uri = uri,
            onDismiss = { cropUri = null },
            onCropped = { bytes -> vm.onEvent(ProfileUiEvent.UploadAvatar(bytes)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarActionSheet(
    hasPhoto: Boolean,
    onDismiss: () -> Unit,
    onView: () -> Unit,
    onUpload: () -> Unit,
    onCamera: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.profile_photo_actions),
                style = MaterialTheme.typography.titleLargeEmphasized,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 2.dp),
            )
            AvatarAction(
                Icons.Filled.Visibility,
                R.string.profile_action_view,
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
                enabled = hasPhoto,
                onClick = onView,
            )
            AvatarAction(
                Icons.Filled.PhotoLibrary,
                R.string.profile_action_upload,
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onUpload,
            )
            AvatarAction(
                Icons.Filled.PhotoCamera,
                R.string.profile_action_camera,
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onCamera,
            )
            AvatarAction(
                Icons.Filled.Delete,
                R.string.profile_action_delete,
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
                enabled = hasPhoto,
                onClick = onDelete,
            )
        }
    }
}

/**
 * One sheet action as a card: a circular tinted icon badge on the left, the label on the
 * right. Each action carries its own container colour so the sheet reads as a set of
 * distinct choices rather than a wall of one tone — Delete is the red one.
 */
@Composable
private fun AvatarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: Int,
    container: Color,
    content: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val dim = if (enabled) 1f else 0.4f
    // The icon badge is the full colour; the label sits on a pale wash of it, so the
    // label must use the *on* colour, not the patch colour — on a near-black patch a
    // dark label would disappear.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(ShapeCache.smooth20)
            .clickable(enabled = enabled, onClick = onClick),
        shape = ShapeCache.smooth20,
        color = container.copy(alpha = 0.38f * dim),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(
                stringResource(label),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier.alpha(dim),
            )
        }
    }
}

@Composable
private fun NotificationsBell(unread: Long, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (unread > 0) {
                    Badge { Text(if (unread > 99) "99+" else unread.toString()) }
                }
            },
        ) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = stringResource(R.string.cd_notifications),
            )
        }
    }
}

@Composable
private fun ProfileStatTile(
    label: String,
    value: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = ShapeCache.smooth18,
        color = container,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = content,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = content.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun EmptySightings(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.AddCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.profile_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SightingTile(s: Sighting, onOpenCat: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = ShapeCache.smooth20,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .aspectRatio(1f)
            .clip(ShapeCache.smooth20)
            .clickable(enabled = s.catId != null) { s.catId?.let(onOpenCat) },
    ) {
        AsyncImage(
            model = s.thumbUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
