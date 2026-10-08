package id.andreasmlbngaol.mpus.auth.ui

import androidx.compose.runtime.Immutable
import id.andreasmlbngaol.mpus.auth.domain.model.AuthTab
import id.andreasmlbngaol.mpus.core.ui.UiText

@Immutable
data class AuthUiState(
    val tab: AuthTab = AuthTab.SignIn,
    // Sign-in and sign-up keep separate credentials on purpose: sharing one pair meant
    // toggling tabs carried a half-typed sign-in email into the signup form (and vice
    // versa). [email]/[password] read whichever pair the active tab owns.
    val signInEmail: String = "",
    val signInPassword: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val username: String = "",
    val nickname: String = "",
    /**
     * The sign-in / sign-up button spinner. Cleared on *every* outcome, including the
     * success paths that leave this screen, so the button can never stay stuck.
     */
    val submitting: Boolean = false,
    val error: UiText? = null,
) {
    val email: String get() = if (tab == AuthTab.SignIn) signInEmail else signUpEmail
    val password: String get() = if (tab == AuthTab.SignIn) signInPassword else signUpPassword
}
