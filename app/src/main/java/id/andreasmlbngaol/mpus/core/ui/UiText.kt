package id.andreasmlbngaol.mpus.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.andreasmlbngaol.mpus.core.domain.model.AppError

/**
 * UI-visible text that is either a localized resource or a dynamic server string.
 *
 * ViewModels never touch Context; they emit [Res] for text they own and [Raw] for text the
 * backend produced. The composable resolves it at render time.
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

/**
 * Maps a domain error to text: a server/validation message shows raw, while a transport
 * failure falls back to a local string. This is why the UI never imports the data layer.
 */
fun AppError.toUiText(@StringRes fallback: Int): UiText = when (this) {
    is AppError.Server -> UiText.Raw(message.orEmpty())
    is AppError.Unauthorized -> UiText.Raw(message.orEmpty())
    is AppError.Network -> UiText.Res(fallback)
    is AppError.Unknown -> UiText.Res(fallback)
}

/** Anything that isn't an [AppError] still maps to the local fallback. */
fun Throwable.toUiText(@StringRes fallback: Int): UiText =
    (this as? AppError)?.toUiText(fallback) ?: UiText.Res(fallback)
