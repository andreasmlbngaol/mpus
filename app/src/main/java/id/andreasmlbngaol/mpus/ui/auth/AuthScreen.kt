package id.andreasmlbngaol.mpus.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.ui.asString
import id.andreasmlbngaol.mpus.ui.components.ExpressiveField
import id.andreasmlbngaol.mpus.ui.resolve
import id.andreasmlbngaol.mpus.ui.theme.ShapeCache
import id.andreasmlbngaol.mpus.ui.theme.SquircleShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AuthScreen(vm: AuthViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.toast.collect { snackbar.showSnackbar(it.resolve(context)) } }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Branding badge: the calico black patch. The page is white and the CTA below
            // is the orange patch, so the badge carries the third colour — otherwise the
            // screen is a wall of orange with no black to anchor it.
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSecondary,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.auth_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            // Expressive connected toggle group: two squircle buttons joined into one
            // pill. The selected one swells into a filled shape — M3's button-group idiom.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                val modes = listOf(
                    AuthTab.SignIn to R.string.auth_sign_in,
                    AuthTab.SignUp to R.string.auth_sign_up,
                )
                modes.forEachIndexed { index, (tab, labelRes) ->
                    ToggleButton(
                        checked = state.tab == tab,
                        onCheckedChange = { vm.onTab(tab) },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            stringResource(labelRes),
                            style = MaterialTheme.typography.titleSmallEmphasized,
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // The form sits in its own squircle card, one step above the page surface.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = SquircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ExpressiveField(
                        value = state.email,
                        onValueChange = vm::onEmail,
                        label = stringResource(R.string.auth_email),
                        leadingIcon = Icons.Filled.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    )
                    ExpressiveField(
                        value = state.password,
                        onValueChange = vm::onPassword,
                        label = stringResource(R.string.auth_password),
                        leadingIcon = Icons.Filled.Lock,
                        visualTransformation = if (showPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        trailingContent = {
                            TextButton(onClick = { showPassword = !showPassword }) {
                                Text(
                                    stringResource(
                                        if (showPassword) R.string.auth_hide else R.string.auth_show,
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        },
                    )

                    AnimatedVisibility(state.tab == AuthTab.SignUp) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ExpressiveField(
                                value = state.username,
                                onValueChange = vm::onUsername,
                                label = stringResource(R.string.auth_username),
                                leadingIcon = Icons.Filled.Person,
                                supportingText = stringResource(R.string.auth_username_hint),
                            )
                            ExpressiveField(
                                value = state.nickname,
                                onValueChange = vm::onNickname,
                                label = stringResource(R.string.auth_display_name),
                                leadingIcon = Icons.Filled.AccountCircle,
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(state.error != null) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeCache.smooth18)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            state.error?.asString().orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = vm::submit,
                enabled = !state.submitting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = SquircleShape,
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                if (state.submitting) {
                    LoadingIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        stringResource(
                            if (state.tab == AuthTab.SignIn) R.string.auth_sign_in
                            else R.string.auth_create_account,
                        ),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                }
            }
        }
    }
}
