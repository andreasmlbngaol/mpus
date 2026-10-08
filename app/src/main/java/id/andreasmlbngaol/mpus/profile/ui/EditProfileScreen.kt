package id.andreasmlbngaol.mpus.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.ui.components.ExpressiveField
import id.andreasmlbngaol.mpus.core.ui.components.MpusTopBar
import id.andreasmlbngaol.mpus.core.ui.resolve
import id.andreasmlbngaol.mpus.core.ui.theme.ShapeCache

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditProfileScreen(vm: EditProfileViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is EditProfileUiEffect.ShowMessage -> snackbar.showSnackbar(effect.text.resolve(context))
            }
        }
    }

    // A successful save closes the editor — the profile tab behind it already shows the
    // new values from the session cache.
    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Scaffold(
        topBar = {
            MpusTopBar(
                title = stringResource(R.string.profile_edit),
                scrollBehavior = scrollBehavior,
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
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ExpressiveField(
                    value = state.nickname,
                    onValueChange = { vm.onEvent(EditProfileUiEvent.NicknameChanged(it)) },
                    label = stringResource(R.string.auth_display_name),
                    leadingIcon = Icons.Filled.Person,
                )
                ExpressiveField(
                    value = state.username,
                    onValueChange = { vm.onEvent(EditProfileUiEvent.UsernameChanged(it)) },
                    label = stringResource(R.string.auth_username),
                    leadingIcon = Icons.Filled.Edit,
                )
                Button(
                    onClick = { vm.onEvent(EditProfileUiEvent.Save) },
                    enabled = !state.busy && state.username.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ShapeCache.smooth18,
                ) { Text(stringResource(R.string.action_save)) }
            }
        }
    }
}
