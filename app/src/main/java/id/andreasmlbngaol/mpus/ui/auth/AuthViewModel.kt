package id.andreasmlbngaol.mpus.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.Session
import id.andreasmlbngaol.mpus.ui.UiText
import id.andreasmlbngaol.mpus.ui.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

enum class AuthTab { SignIn, SignUp }

@androidx.compose.runtime.Immutable
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
     * success paths that leave this screen (login and signup both save a session), so
     * the button can never stay stuck.
     */
    val submitting: Boolean = false,
    val error: UiText? = null,
) {
    val email: String get() = if (tab == AuthTab.SignIn) signInEmail else signUpEmail
    val password: String get() = if (tab == AuthTab.SignIn) signInPassword else signUpPassword
}

@KoinViewModel
class AuthViewModel(
    private val api: ApiClient,
    private val session: Session,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _toast = Channel<UiText>(Channel.BUFFERED)
    val toast = _toast.receiveAsFlow()

    fun onTab(tab: AuthTab) = _state.update { it.copy(tab = tab, error = null) }

    fun onEmail(v: String) = _state.update {
        if (it.tab == AuthTab.SignIn) it.copy(signInEmail = v, error = null)
        else it.copy(signUpEmail = v, error = null)
    }

    fun onPassword(v: String) = _state.update {
        if (it.tab == AuthTab.SignIn) it.copy(signInPassword = v, error = null)
        else it.copy(signUpPassword = v, error = null)
    }

    fun onUsername(v: String) = _state.update { it.copy(username = v, error = null) }
    fun onNickname(v: String) = _state.update { it.copy(nickname = v, error = null) }

    fun submit() {
        if (_state.value.tab == AuthTab.SignUp) signup() else login()
    }

    private fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = UiText.Res(R.string.auth_err_missing_credentials)) }
            return
        }
        submit(R.string.auth_err_network) {
            val data = api.login(s.email.trim(), s.password)
            session.save(data.token, data.user)
            // This VM is activity-scoped, so it outlives the sign-out. Clear the form now
            // that we're signed in, otherwise the old email/password linger on the next
            // signed-out visit until the app is killed.
            _state.value = AuthUiState()
        }
    }

    private fun signup() {
        val s = _state.value
        if (s.email.isBlank() || s.password.length < 8 || s.username.length < 3 || s.nickname.isBlank()) {
            _state.update { it.copy(error = UiText.Res(R.string.auth_err_incomplete)) }
            return
        }
        submit(R.string.auth_err_network) {
            // Signing up signs you in. The session lands the app on the verify screen
            // (the new user has `email_verified == false`), not back on this form.
            val data = api.signup(s.email.trim(), s.password, s.username.trim(), s.nickname.trim())
            session.save(data.token, data.user)
            _state.value = AuthUiState()
        }
    }

    /**
     * Runs a submit action and always clears [AuthUiState.submitting], whether the action
     * succeeds (and may navigate away), fails, or throws.
     */
    private fun submit(fallback: Int, block: suspend () -> Unit) {
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.toUiText(fallback)) }
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }
}
