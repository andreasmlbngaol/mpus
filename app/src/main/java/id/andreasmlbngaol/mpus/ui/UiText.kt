package id.andreasmlbngaol.mpus.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.andreasmlbngaol.mpus.data.ApiException

/**
 * UI-visible text that is either a localized resource or a dynamic server string.
 *
 * ViewModels never touch Context; they emit [Res] for text they own and [Raw] for
 * text the backend produced. The composable resolves it at render time.
 */
sealed interface UiText {
    data class Res(@StringRes val id: Int) : UiText
    data class Raw(val value: String) : UiText
}

/** For composition. */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Res -> stringResource(id)
    is UiText.Raw -> value
}

/** For coroutine / effect lambdas where no @Composable scope is available. */
fun UiText.resolve(context: Context): String = when (this) {
    is UiText.Res -> context.getString(id)
    is UiText.Raw -> value
}

/** Maps a thrown exception to text: server message when available, else a local fallback. */
fun Throwable.toUiText(@StringRes fallback: Int): UiText =
    if (this is ApiException) UiText.Raw(message) else UiText.Res(fallback)
