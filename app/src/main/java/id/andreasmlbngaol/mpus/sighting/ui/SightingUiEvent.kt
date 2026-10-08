package id.andreasmlbngaol.mpus.sighting.ui

import android.net.Uri
import id.andreasmlbngaol.mpus.core.ui.UiText

/** Everything the user can do on the Snap screen. */
sealed interface SightingUiEvent {
    data class PhotoCaptured(val uri: Uri) : SightingUiEvent
    data object CameraDenied : SightingUiEvent
    data object Locate : SightingUiEvent

    /** The screen reads the photo's bytes (it owns the ContentResolver) and hands them over. */
    data class Upload(val bytes: ByteArray) : SightingUiEvent

    data class LinkTo(val catId: String) : SightingUiEvent
    data class NameNew(val name: String) : SightingUiEvent
    data object Reset : SightingUiEvent
}

/** One-shot outputs the screen reacts to once. */
sealed interface SightingUiEffect {
    data class ShowMessage(val text: UiText) : SightingUiEffect
}
