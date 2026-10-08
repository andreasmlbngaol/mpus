package id.andreasmlbngaol.mpus.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.auth.domain.model.AuthTab
import id.andreasmlbngaol.mpus.auth.domain.usecase.AuthUseCase
import id.andreasmlbngaol.mpus.core.domain.usecase.SessionUseCase
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.core.ui.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AuthViewModel(
    private val auth: AuthUseCase,
    private val session: SessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _effect = Channel<AuthUiEffect>(Channel.BUFFERED)
    val effect: Flow<AuthUiEffect> = _effect.receiveAsFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.SelectTab -> _state.update { it.copy(tab = event.tab, error = null) }
            is AuthUiEvent.EmailChanged -> _state.update {
                if (it.tab == AuthTab.SignIn) it.copy(signInEmail = event.value, error = null)
                else it.copy(signUpEmail = event.value, error = null)
            }
            is AuthUiEvent.PasswordChanged -> _state.update {
                if (it.tab == AuthTab.SignIn) it.copy(signInPassword = event.value, error = null)
                else it.copy(signUpPassword = event.value, error = null)
            }
            is AuthUiEvent.UsernameChanged -> _state.update { it.copy(username = event.value, error = null) }
            is AuthUiEvent.NicknameChanged -> _state.update { it.copy(nickname = event.value, error = null) }
            AuthUiEvent.Submit -> if (_state.value.tab == AuthTab.SignUp) signup() else login()
        }
    }

    private fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = UiText.Res(R.string.auth_err_missing_credentials)) }
            return
        }
        submit(R.string.auth_err_network) {
            val data = auth.login(s.email.trim(), s.password)
            session.save(data.token, data.user)
            // This VM is activity-scoped, so it outlives the sign-out. Clear the form now
            // that we're signed in, otherwise the old email/password linger on the next
            // signed-out visit until the app is killed.
            _state.update { AuthUiState() }
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
            val data = auth.signup(s.email.trim(), s.password, s.username.trim(), s.nickname.trim())
            session.save(data.token, data.user)
            _state.update { AuthUiState() }
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
