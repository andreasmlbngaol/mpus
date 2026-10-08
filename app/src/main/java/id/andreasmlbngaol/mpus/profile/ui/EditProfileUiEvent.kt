package id.andreasmlbngaol.mpus.profile.ui

import id.andreasmlbngaol.mpus.core.ui.UiText

/** Everything the user can do on the edit-profile screen. */
sealed interface EditProfileUiEvent {
    data class UsernameChanged(val value: String) : EditProfileUiEvent
    data class NicknameChanged(val value: String) : EditProfileUiEvent
    data object Save : EditProfileUiEvent
}

sealed interface EditProfileUiEffect {
    data class ShowMessage(val text: UiText) : EditProfileUiEffect
}
