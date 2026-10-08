package id.andreasmlbngaol.mpus.auth.ui

import id.andreasmlbngaol.mpus.auth.domain.model.AuthTab
import id.andreasmlbngaol.mpus.core.ui.UiText

/** Everything the user can do on the auth screen. */
sealed interface AuthUiEvent {
    data class SelectTab(val tab: AuthTab) : AuthUiEvent
    data class EmailChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data class UsernameChanged(val value: String) : AuthUiEvent
    data class NicknameChanged(val value: String) : AuthUiEvent
    data object Submit : AuthUiEvent
}

/** One-shot outputs the screen reacts to once. */
sealed interface AuthUiEffect {
    data class ShowMessage(val text: UiText) : AuthUiEffect
}
