package id.andreasmlbngaol.mpus.cat.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.cat.domain.model.CatDetail
import id.andreasmlbngaol.mpus.cat.domain.model.CatName
import id.andreasmlbngaol.mpus.cat.domain.model.CatReview
import id.andreasmlbngaol.mpus.cat.domain.model.MergeTarget
import id.andreasmlbngaol.mpus.core.ui.asString
import id.andreasmlbngaol.mpus.core.ui.components.ExpressiveField
import id.andreasmlbngaol.mpus.core.ui.components.ExpressiveState
import id.andreasmlbngaol.mpus.core.ui.resolve
import id.andreasmlbngaol.mpus.core.ui.theme.ShapeCache
import id.andreasmlbngaol.mpus.core.ui.theme.SquircleShape
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CatScreen(vm: CatViewModel, onBack: () -> Unit, onOpenUser: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is CatUiEffect.ShowMessage -> snackbar.showSnackbar(effect.text.resolve(context))
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // A flag is a report, not an instant action — confirm before it goes anywhere.
    var reportTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    val pendingReport = reportTarget
    if (pendingReport != null) {
        val (kind, targetId) = pendingReport
        AlertDialog(
            onDismissRequest = { reportTarget = null },
            shape = SquircleShape,
            title = { Text(stringResource(R.string.report_title)) },
            text = { Text(stringResource(R.string.report_body)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.onEvent(CatUiEvent.Report(kind, targetId))
                    reportTarget = null
                }) { Text(stringResource(R.string.report_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { reportTarget = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
    val detail = state.detail

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // The header itself collapses: the big photo scrolls away and a thumbnail of it
            // shrinks into the top-left of the bar, with the name beside it — so the cat is
            // never hidden behind its own photo once reviews start scrolling in.
            CollapsingCatHeader(
                photoUrl = detail?.sightings?.firstOrNull()?.thumbUrl,
                title = detail?.displayName ?: stringResource(R.string.cat_title),
                subtitle = detail?.let {
                    pluralStringResource(R.plurals.cat_sighting_count, it.sightings.size, it.sightings.size)
                },
                scrollBehavior = scrollBehavior,
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            val error = state.error
            when {
                state.loading -> ContainedLoadingIndicator(Modifier.align(Alignment.Center))
                error != null -> ExpressiveState(
                    message = stringResource(R.string.cat_err_load),
                    description = error.asString(),
                    onRetry = { vm.onEvent(CatUiEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                )
                detail != null -> Detail(
                    detail = detail,
                    busy = state.busy,
                    mergeTargets = state.mergeTargets,
                    loadingTargets = state.loadingTargets,
                    onLike = { vm.onEvent(CatUiEvent.NameLiked(it)) },
                    onAddName = { vm.onEvent(CatUiEvent.NameSubmitted(it)) },
                    onAddReview = { body, rating -> vm.onEvent(CatUiEvent.ReviewSubmitted(body, rating)) },
                    onLikeReview = { vm.onEvent(CatUiEvent.ReviewLiked(it)) },
                    onReportName = { reportTarget = "name" to it },
                    onReportReview = { reportTarget = "review" to it },
                    onLoadMergeTargets = { vm.onEvent(CatUiEvent.LoadMergeTargets) },
                    onMerge = { vm.onEvent(CatUiEvent.MergeInto(it)) },
                    onOpenUser = onOpenUser,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingCatHeader(
    photoUrl: String?,
    title: String,
    subtitle: String?,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
) {
    // 0 = expanded (no thumbnail), 1 = collapsed (thumbnail at full size). Reading the
    // scroll fraction here re-lays out the bar as the list moves.
    val fraction = scrollBehavior.state.collapsedFraction
    val thumb = (48.dp * fraction).coerceAtLeast(0.dp)
    LargeFlexibleTopAppBar(
        title = { Text(title) },
        subtitle = subtitle?.let { { Text(it) } },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                if (photoUrl != null && fraction > 0.05f) {
                    Spacer(Modifier.width(12.dp))
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(thumb)
                            .clip(ShapeCache.smooth16)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                    )
                }
            }
        },
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Detail(
    detail: CatDetail,
    busy: Boolean,
    mergeTargets: List<MergeTarget>,
    loadingTargets: Boolean,
    onLike: (String) -> Unit,
    onAddName: (String) -> Unit,
    onAddReview: (String, Int) -> Unit,
    onLikeReview: (String) -> Unit,
    onReportName: (String) -> Unit,
    onReportReview: (String) -> Unit,
    onLoadMergeTargets: () -> Unit,
    onMerge: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var mergeOpen by remember { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        // Hero photo, squircle-cropped like every other image surface in the app.
        if (detail.sightings.isNotEmpty()) {
            AsyncImage(
                model = detail.sightings.first().photoUrl,
                contentDescription = detail.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(ShapeCache.smooth28)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            )
            Spacer(Modifier.height(20.dp))
        }

        Text(
            detail.displayName ?: stringResource(R.string.cat_not_named),
            style = MaterialTheme.typography.headlineMediumEmphasized,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            pluralStringResource(R.plurals.cat_sighting_count, detail.sightings.size, detail.sightings.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Stat tiles give the page a body — counts of what this cat has gathered.
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CatStatTile(
                label = stringResource(R.string.cat_stat_sightings),
                value = detail.sightings.size.toString(),
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            CatStatTile(
                label = stringResource(R.string.cat_stat_names),
                value = detail.names.size.toString(),
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
            CatStatTile(
                label = stringResource(R.string.cat_reviews),
                value = detail.reviews.size.toString(),
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
        }

        if (detail.sightings.size > 1) {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.cat_photos),
                style = MaterialTheme.typography.titleMediumEmphasized,
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(detail.sightings, key = { it.id }) { s ->
                    AsyncImage(
                        model = s.thumbUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(ShapeCache.smooth20)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.cat_names),
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        Spacer(Modifier.height(8.dp))
        if (detail.names.isEmpty()) {
            Text(
                stringResource(R.string.cat_no_names),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // Rank by likes so the list reads as a leaderboard, not a plain stack.
            val ranked = detail.names.sortedByDescending { it.likes }
            ranked.forEachIndexed { index, n ->
                NameRow(
                    n,
                    rank = index + 1,
                    onLike = { onLike(n.id) },
                    onReport = { onReportName(n.id) },
                    onOpenUser = { onOpenUser(n.userId) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        // Naming and reviewing are for people who've actually met the cat — you can't
        // judge one you only saw on the map. Once you've named it, the composer goes away;
        // you can still like the other names on the list above.
        val myName = detail.names.firstOrNull { it.mine }
        if (myName != null) {
            Surface(
                shape = ShapeCache.smooth18,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.cat_you_named, myName.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        } else if (detail.canContribute) {
            ExpressiveField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.cat_suggest_name),
                leadingIcon = Icons.Filled.Person,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onAddName(name); name = "" },
                enabled = name.isNotBlank() && !busy,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = ShapeCache.smooth18,
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) { Text(stringResource(R.string.cat_save_name), style = MaterialTheme.typography.titleSmallEmphasized) }
        } else {
            Text(
                stringResource(R.string.cat_meet_to_name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Reviews: a note + 0-10 rating per person, each independently likeable.
        Spacer(Modifier.height(32.dp))
        Text(
            stringResource(R.string.cat_reviews),
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        Spacer(Modifier.height(12.dp))
        val myReview = detail.reviews.firstOrNull { it.mine }
        when {
            myReview != null -> {
                Text(
                    stringResource(R.string.cat_you_reviewed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
            }
            detail.canContribute -> {
                ReviewComposer(enabled = !busy, onSubmit = onAddReview)
                Spacer(Modifier.height(16.dp))
            }
        }
        if (detail.reviews.isEmpty()) {
            Text(
                stringResource(R.string.cat_no_reviews),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            detail.reviews.forEach { r ->
                ReviewCard(
                    r,
                    onLike = { onLikeReview(r.id) },
                    onReport = { onReportReview(r.id) },
                    onOpenUser = { onOpenUser(r.userId) },
                )
            }
        }

        // Merge: two records that are really one cat. Collapsed behind a quiet text
        // button so it never competes with the everyday actions.
        Spacer(Modifier.height(32.dp))
        TextButton(
            onClick = {
                mergeOpen = !mergeOpen
                if (mergeOpen) onLoadMergeTargets()
            },
            enabled = !busy,
        ) { Text(stringResource(R.string.merge_into)) }
        AnimatedVisibility(mergeOpen) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (loadingTargets) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        ContainedLoadingIndicator()
                    }
                } else if (mergeTargets.isEmpty()) {
                    Text(
                        stringResource(R.string.user_no_cats),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    mergeTargets.forEach { t ->
                        MergeTargetRow(t, enabled = !busy, onClick = { onMerge(t.id) })
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun MergeTargetRow(t: MergeTarget, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = ShapeCache.smooth20,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeCache.smooth20)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (t.thumbUrl != null) {
                AsyncImage(
                    model = t.thumbUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(ShapeCache.smooth12)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                t.name ?: stringResource(R.string.cat_not_named),
                style = MaterialTheme.typography.titleSmallEmphasized,
            )
        }
    }
}

@Composable
private fun ReviewComposer(enabled: Boolean, onSubmit: (String, Int) -> Unit) {
    var body by remember { mutableStateOf("") }
    var rating by remember { mutableFloatStateOf(8f) }
    Surface(
        shape = ShapeCache.smooth20,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExpressiveField(
                value = body,
                onValueChange = { body = it },
                label = stringResource(R.string.cat_review_hint),
                leadingIcon = Icons.Filled.RateReview,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.cat_review_rating),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(R.string.cat_review_rating_value, rating.roundToInt()),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 0f..10f,
                steps = 9,
            )
            Button(
                onClick = { onSubmit(body, rating.roundToInt()); body = "" },
                enabled = enabled && body.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = ShapeCache.smooth18,
            ) { Text(stringResource(R.string.cat_review_save)) }
        }
    }
}

@Composable
private fun ReviewCard(r: CatReview, onLike: () -> Unit, onReport: () -> Unit, onOpenUser: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = ShapeCache.smooth20,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        r.nickname,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        modifier = Modifier.clickable(onClick = onOpenUser),
                    )
                }
                // Rating as a colour-coded pill: warm for high, muted for low. Returns its
                // own content colour too — the mid patch is near-black, so a fixed text
                // colour would vanish on it.
                val (ratingBg, ratingFg) = ratingColors(r.rating)
                Surface(
                    shape = ShapeCache.smoothPill,
                    color = ratingBg,
                ) {
                    Text(
                        stringResource(R.string.cat_review_rating_value, r.rating),
                        style = MaterialTheme.typography.labelLarge,
                        color = ratingFg,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(r.body, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onReport) {
                    Icon(
                        Icons.Filled.Flag,
                        contentDescription = stringResource(R.string.report_review),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Count sits under the heart so it's unmistakably this review's like count.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onLike) {
                        Icon(
                            if (r.likedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(R.string.cd_like),
                            tint = if (r.likedByMe) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        r.likes.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (r.likedByMe) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** A 0-10 rating reads warmer the higher it is. Returns (background, content). */
@Composable
private fun ratingColors(rating: Int): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val cs = MaterialTheme.colorScheme
    return when {
        rating >= 7 -> cs.tertiaryContainer to cs.onTertiaryContainer
        rating >= 4 -> cs.secondaryContainer to cs.onSecondaryContainer
        else -> cs.errorContainer to cs.onErrorContainer
    }
}

@Composable
private fun CatStatTile(
    label: String,
    value: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
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
private fun NameRow(n: CatName, rank: Int, onLike: () -> Unit, onReport: () -> Unit, onOpenUser: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = ShapeCache.smooth20,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Rank badge: the number in a tinted squircle, the row's leading anchor.
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(ShapeCache.smooth12)
                    .background(
                        if (rank == 1) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    rank.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rank == 1) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(n.name, style = MaterialTheme.typography.titleMediumEmphasized)
                Text(
                    stringResource(R.string.cat_by, n.nickname),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onOpenUser),
                )
            }
            IconButton(onClick = onReport) {
                Icon(
                    Icons.Filled.Flag,
                    contentDescription = stringResource(R.string.report_name),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Count sits under the heart so it's unmistakably this name's like count.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onLike) {
                    Icon(
                        if (n.likedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_like),
                        tint = if (n.likedByMe) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    n.likes.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (n.likedByMe) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
