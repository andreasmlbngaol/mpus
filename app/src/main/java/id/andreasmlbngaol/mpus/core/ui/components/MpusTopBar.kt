package id.andreasmlbngaol.mpus.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * The one app bar every tab uses. PixelPlayer gives each screen the *same* collapsible
 * header (large title that shrinks on scroll, optional subtitle) — MPUS was mixing a small
 * pinned bar on the map with large flexible bars elsewhere, which is what made the tabs
 * feel like different apps.
 *
 * Pass the same [scrollBehavior] the screen's scroll container is wired to; a screen with no
 * scroll source (the map) simply leaves it null and stays expanded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MpusTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = Color.Unspecified,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    LargeFlexibleTopAppBar(
        title = { Text(title) },
        subtitle = subtitle?.let { { Text(it) } },
        navigationIcon = navigationIcon,
        actions = actions,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = if (containerColor == Color.Unspecified) {
            TopAppBarDefaults.topAppBarColors()
        } else {
            TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                scrolledContainerColor = containerColor,
            )
        },
    )
}
