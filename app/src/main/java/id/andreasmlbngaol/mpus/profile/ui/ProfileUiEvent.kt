package id.andreasmlbngaol.mpus.profile.ui

import id.andreasmlbngaol.mpus.core.ui.UiText

/** Everything the user can do on the "You" tab. */
sealed interface ProfileUiEvent {
    data object Refresh : ProfileUiEvent
    data object RefreshUnread : ProfileUiEvent
    data object LoadMore : ProfileUiEvent

    /** The screen reads the cropped bytes and hands them over. */
    data class UploadAvatar(val bytes: ByteArray) : ProfileUiEvent

    data object DeleteAvatar : ProfileUiEvent
    data object Logout : ProfileUiEvent
}

/** One-shot outputs the screen reacts to once. */
sealed interface ProfileUiEffect {
    data class ShowMessage(val text: UiText) : ProfileUiEffect
}
