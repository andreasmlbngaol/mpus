package id.andreasmlbngaol.mpus.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.PushRegistrar
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

/**
 * The gate an unverified account sits behind after signing in or signing up. It owns the
 * code field, the resend action and the way out (sign out). Nothing else in the app is
 * reachable until [Session.user] flips `emailVerified` to true — which the session update
 * after a successful verify does, and `App.kt` watches.
 */
@androidx.compose.runtime.Immutable
data class VerifyUiState(
    val code: String = "",
    val busy: Boolean = false,
    val error: UiText? = null,
)

@KoinViewModel
class VerifyEmailViewModel(
    private val api: ApiClient,
    private val session: Session,
    private val registrar: PushRegistrar,
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyUiState())
    val state: StateFlow<VerifyUiState> = _state.asStateFlow()

    private val _toast = Channel<UiText>(Channel.BUFFERED)
    val toast = _toast.receiveAsFlow()

    val email: String get() = session.user.value?.email.orEmpty()

    fun onCode(v: String) = _state.update { it.copy(code = v, error = null) }

    fun verify() {
        val code = _state.value.code.trim()
        if (code.isBlank()) return
        run(R.string.verify_err) {
            val user = api.verifyEmail(code)
            session.updateUser(user)
            _toast.trySend(UiText.Res(R.string.verify_done))
        }
    }

    fun resend() {
        val email = email
        if (email.isBlank()) return
        run(R.string.verify_err) {
            _toast.trySend(UiText.Raw(api.resendVerification(email)))
        }
    }

    fun logout() {
        viewModelScope.launch {
            registrar.unregisterCurrent()
            runCatching { api.logout() }
            session.clear()
        }
    }

    private fun run(fallback: Int, block: suspend () -> Unit) {
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                block()
                _state.update { it.copy(busy = false) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.toUiText(fallback)) }
            }
        }
    }
}
