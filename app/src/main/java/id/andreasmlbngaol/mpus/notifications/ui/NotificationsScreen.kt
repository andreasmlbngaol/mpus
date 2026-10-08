package id.andreasmlbngaol.mpus.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.domain.model.MergeRequest
import id.andreasmlbngaol.mpus.core.ui.asString
import id.andreasmlbngaol.mpus.core.ui.components.ExpressiveState
import id.andreasmlbngaol.mpus.core.ui.components.MpusTopBar
import id.andreasmlbngaol.mpus.core.ui.resolve
import id.andreasmlbngaol.mpus.core.ui.theme.ShapeCache
import id.andreasmlbngaol.mpus.notifications.domain.model.AppNotification

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationsScreen(
    vm: NotificationsViewModel,
    onBack: () -> Unit,
    onOpenCat: (String) -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is NotificationsUiEffect.ShowMessage -> snackbar.showSnackbar(effect.text.resolve(context))
            }
        }
    }

    Scaffold(
        topBar = {
            MpusTopBar(
                title = stringResource(R.string.notifications_title),
                navigationIcon = {
                    FilledIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            val error = state.error
            when {
                state.loading -> ContainedLoadingIndicator(Modifier.align(Alignment.Center))
                error != null -> ExpressiveState(
                    message = stringResource(R.string.notif_err_load),
                    description = error.asString(),
                    onRetry = { vm.onEvent(NotificationsUiEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                )
                state.items.isEmpty() && state.merges.isEmpty() -> EmptyInbox(Modifier.fillMaxSize())
                else -> {
                    val listState = rememberLazyListState()
                    val nearEnd by remember {
                        derivedStateOf {
                            val layout = listState.layoutInfo
                            val last = layout.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
                            layout.totalItemsCount > 0 && last >= layout.totalItemsCount - 4
                        }
                    }
                    LaunchedEffect(nearEnd, state.nextCursor) {
                        if (nearEnd) vm.onEvent(NotificationsUiEvent.LoadMore)
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.merges.isNotEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.merge_pending_title),
                                    style = MaterialTheme.typography.titleMediumEmphasized,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                                )
                            }
                            items(state.merges, key = { it.id }) { m ->
                                MergeCard(
                                    m,
                                    onApprove = { vm.onEvent(NotificationsUiEvent.Approve(m.id)) },
                                    onReject = { vm.onEvent(NotificationsUiEvent.Reject(m.id)) },
                                )
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                        items(state.items, key = { it.id }) { n ->
                            NotificationCard(n, onOpenCat = onOpenCat)
                        }
                        if (state.loadingMore) {
                            item {
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
}

@Composable
private fun NotificationCard(n: AppNotification, onOpenCat: (String) -> Unit) {
    val (icon, container, content) = notificationStyle(n.kind)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeCache.smooth20)
            .clickable(enabled = n.catId != null) { n.catId?.let(onOpenCat) },
        shape = ShapeCache.smooth20,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ActorAvatar(n.actorAvatarUrl, n.actorNickname)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    notificationText(n),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (!n.seen) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ActorAvatar(url: String?, nickname: String?) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize())
        } else if (!nickname.isNullOrBlank()) {
            Text(
                nickname.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MergeCard(m: MergeRequest, onApprove: () -> Unit, onReject: () -> Unit) {
    Surface(
        shape = ShapeCache.smooth20,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.CallMerge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(
                        R.string.merge_row,
                        m.sourceName ?: stringResource(R.string.cat_not_named),
                        m.targetName ?: stringResource(R.string.cat_not_named),
                    ),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = ShapeCache.smooth18,
                ) { Text(stringResource(R.string.action_done)) }
                TextButton(
                    onClick = onReject,
                    modifier = Modifier.height(44.dp),
                ) { Text(stringResource(R.string.action_cancel)) }
            }
        }
    }
}

@Composable
private fun EmptyInbox(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Notifications,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.notif_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun notificationText(n: AppNotification): String {
    val actor = n.actorNickname ?: stringResource(R.string.notif_someone)
    val cat = n.catName ?: stringResource(R.string.notif_this_cat)
    val line = when (n.kind) {
        "name_liked" -> stringResource(R.string.notif_liked_name, cat)
        "name_added" -> stringResource(R.string.notif_added_name, cat)
        "review_added" -> stringResource(R.string.notif_added_review, cat)
        "merge_requested" -> stringResource(R.string.notif_merge_requested, cat)
        "merge_resolved" -> stringResource(R.string.notif_merge_resolved, cat)
        else -> cat
    }
    return "$actor $line"
}

@Composable
private fun notificationStyle(kind: String): Triple<ImageVector, Color, Color> {
    val cs = MaterialTheme.colorScheme
    return when (kind) {
        "name_liked" -> Triple(Icons.Filled.Favorite, cs.primaryContainer, cs.onPrimaryContainer)
        "name_added" -> Triple(Icons.Filled.Person, cs.secondaryContainer, cs.onSecondaryContainer)
        "review_added" -> Triple(Icons.Filled.RateReview, cs.tertiaryContainer, cs.onTertiaryContainer)
        "merge_requested" -> Triple(Icons.Filled.CallMerge, cs.primaryContainer, cs.onPrimaryContainer)
        else -> Triple(Icons.Filled.Notifications, cs.surfaceContainerHigh, cs.onSurfaceVariant)
    }
}
