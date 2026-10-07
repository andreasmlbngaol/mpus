package id.andreasmlbngaol.mpus.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.DeepLinks
import id.andreasmlbngaol.mpus.data.PushRegistrar
import id.andreasmlbngaol.mpus.data.SessionStore
import id.andreasmlbngaol.mpus.ui.auth.AuthScreen
import id.andreasmlbngaol.mpus.ui.auth.AuthViewModel
import id.andreasmlbngaol.mpus.ui.auth.VerifyEmailScreen
import id.andreasmlbngaol.mpus.ui.auth.VerifyEmailViewModel
import id.andreasmlbngaol.mpus.ui.cat.CatScreen
import id.andreasmlbngaol.mpus.ui.cat.CatViewModel
import id.andreasmlbngaol.mpus.ui.map.MapScreen
import id.andreasmlbngaol.mpus.ui.map.MapViewModel
import id.andreasmlbngaol.mpus.ui.notifications.NotificationsScreen
import id.andreasmlbngaol.mpus.ui.notifications.NotificationsViewModel
import id.andreasmlbngaol.mpus.ui.profile.EditProfileScreen
import id.andreasmlbngaol.mpus.ui.profile.EditProfileViewModel
import id.andreasmlbngaol.mpus.ui.profile.ProfileScreen
import id.andreasmlbngaol.mpus.ui.profile.ProfileViewModel
import id.andreasmlbngaol.mpus.ui.sighting.SightingsScreen
import id.andreasmlbngaol.mpus.ui.sighting.SightingsViewModel
import id.andreasmlbngaol.mpus.ui.theme.sharedAxisEnter
import id.andreasmlbngaol.mpus.ui.theme.sharedAxisExit
import id.andreasmlbngaol.mpus.ui.theme.sharedAxisPopEnter
import id.andreasmlbngaol.mpus.ui.theme.sharedAxisPopExit
import id.andreasmlbngaol.mpus.ui.theme.sharedAxisPredictivePopEnter
import id.andreasmlbngaol.mpus.ui.theme.sharedAxisPredictivePopExit
import id.andreasmlbngaol.mpus.ui.user.UserScreen
import id.andreasmlbngaol.mpus.ui.user.UserViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private enum class Tab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Map("map", R.string.tab_map, Icons.Filled.Place),
    Snap("snap", R.string.tab_snap, Icons.Filled.Add),
    Profile("profile", R.string.tab_you, Icons.Filled.Person),
}

private const val CAT_ROUTE = "cat/{id}"
private const val USER_ROUTE = "user/{id}"
private const val NOTIFICATIONS_ROUTE = "notifications"
private const val EDIT_PROFILE_ROUTE = "edit-profile"

@Composable
fun MpusApp() {
    val session: SessionStore = koinInject()
    val token by session.token.collectAsStateWithLifecycle()
    val registrar: PushRegistrar = koinInject()

    // Claim this device's FCM token for whoever just signed in (and re-claim on a cold
    // start while already signed in). Rotation is handled by MpusMessagingService.
    LaunchedEffect(token) {
        if (token != null) registrar.registerCurrent()
    }

    // Ask for precise location on first open: the whole app is map- and sighting-centric,
    // and the system only shows the precise/approximate choice if we request FINE first.
    // A denied result is fine — Snap degrades to "no location" and the user can retry.
    // POST_NOTIFICATIONS rides along (Android 13+): a denied result just means pushes stay
    // silent, the in-app inbox still works.
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {}
    LaunchedEffect(Unit) {
        locationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
            ),
        )
    }

    if (token == null) {
        // Activity-scoped, so the in-progress form survives rotation.
        val vm: AuthViewModel = koinViewModel()
        AuthScreen(vm)
    } else {
        // An unverified account can sign in, but it stops here until the code is entered.
        // Verifying updates the session user (email_verified true), which flips this gate.
        val user by session.user.collectAsStateWithLifecycle()
        if (user?.emailVerified == false) {
            val vm: VerifyEmailViewModel = koinViewModel()
            VerifyEmailScreen(vm)
        } else {
            SignedInApp()
        }
    }
}

@Composable
private fun SignedInApp() {
    val session: SessionStore = koinInject()
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val tabs = Tab.entries
    val showBar = current in tabs.map { it.route }

    // A tapped push drops its target here. Open the cat it's about, or fall back to the
    // inbox for anything without a cat. Consumed once, and only while signed in — a tap
    // that arrives before login waits here until the session appears.
    val pending by DeepLinks.pending.collectAsStateWithLifecycle()
    LaunchedEffect(pending) {
        val target = pending ?: return@LaunchedEffect
        if (target.catId != null) nav.navigate("cat/${target.catId}") else nav.navigate(NOTIFICATIONS_ROUTE)
        DeepLinks.consume()
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = showBar, enter = fadeIn(), exit = fadeOut()) {
                // Expressive floating pill: rounded, inset from the edges, hovering above
                // the gesture bar. The bar owns its own bottom inset via navigationBarsPadding,
                // so the content below reserves that height through the scaffold padding.
                Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    ShortNavigationBar(
                        // The outer Box already reserves the gesture-bar inset, so the bar
                        // itself must not add it again.
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        // No fillMaxWidth here: it would force a full-width minWidth onto each
                        // item and break the equal-weight distribution. The bar's own measure
                        // policy already fills the incoming width.
                        modifier = Modifier.clip(MaterialTheme.shapes.extraLarge),
                    ) {
                        tabs.forEach { tab ->
                            ShortNavigationBarItem(
                                selected = current == tab.route,
                                onClick = { nav.switchTab(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                                label = { Text(stringResource(tab.labelRes)) },
                            )
                        }
                    }
                }
            }
        },
    ) { pad ->
        // Reserve only the space the bottom navigation bar occupies, and mark it as
        // consumed so screens do not add the navigation-bar inset a second time.
        // The top is left unpadded on purpose: each screen's app bar draws behind the
        // status bar and applies that inset itself, which is what edge-to-edge wants.
        val bottomPad = PaddingValues(bottom = pad.calculateBottomPadding())
        NavHost(
            navController = nav,
            startDestination = Tab.Map.route,
            modifier = Modifier.padding(bottomPad).consumeWindowInsets(bottomPad),
            enterTransition = { sharedAxisEnter() },
            exitTransition = { sharedAxisExit() },
            popEnterTransition = { sharedAxisPopEnter() },
            popExitTransition = { sharedAxisPopExit() },
            predictivePopEnterTransition = { sharedAxisPredictivePopEnter() },
            predictivePopExitTransition = { sharedAxisPredictivePopExit() },
        ) {
            composable(Tab.Map.route) { entry ->
                val vm: MapViewModel = koinViewModel(viewModelStoreOwner = entry)
                MapScreen(vm, onOpenCat = { nav.navigate("cat/$it") })
            }
            composable(Tab.Snap.route) { entry ->
                val vm: SightingsViewModel = koinViewModel(viewModelStoreOwner = entry)
                SightingsScreen(vm, onOpenCat = { nav.navigate("cat/$it") })
            }
            composable(Tab.Profile.route) { entry ->
                val vm: ProfileViewModel = koinViewModel(viewModelStoreOwner = entry)
                val user by session.user.collectAsStateWithLifecycle()
                ProfileScreen(
                    vm,
                    user,
                    onOpenCat = { nav.navigate("cat/$it") },
                    onOpenNotifications = { nav.navigate(NOTIFICATIONS_ROUTE) },
                    onEditProfile = { nav.navigate(EDIT_PROFILE_ROUTE) },
                )
            }
            composable(CAT_ROUTE) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val vm: CatViewModel = koinViewModel(
                    viewModelStoreOwner = entry,
                    key = id,
                ) { parametersOf(id) }
                CatScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onOpenUser = { nav.navigate("user/$it") },
                )
            }
            composable(USER_ROUTE) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val vm: UserViewModel = koinViewModel(
                    viewModelStoreOwner = entry,
                    key = id,
                ) { parametersOf(id) }
                UserScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onOpenCat = { nav.navigate("cat/$it") },
                )
            }
            composable(NOTIFICATIONS_ROUTE) { entry ->
                val vm: NotificationsViewModel = koinViewModel(viewModelStoreOwner = entry)
                NotificationsScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onOpenCat = { nav.navigate("cat/$it") },
                )
            }
            composable(EDIT_PROFILE_ROUTE) { entry ->
                val vm: EditProfileViewModel = koinViewModel(viewModelStoreOwner = entry)
                EditProfileScreen(vm, onBack = { nav.popBackStack() })
            }
        }
    }
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
