package id.andreasmlbngaol.mpus.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.auth.domain.usecase.AuthUseCase
import id.andreasmlbngaol.mpus.core.domain.repository.PushRepository
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

/**
 * The gate an unverified account sits behind after signing in or signing up. It owns the
 * code field, the resend action and the way out (sign out). Nothing else in the app is
 * reachable until the session user flips `emailVerified` to true.
 */
@androidx.compose.runtime.Immutable
data class VerifyUiState(
    val email: String = "",
    val code: String = "",
    val busy: Boolean = false,
    val error: UiText? = null,
)

sealed interface VerifyUiEvent {
    data class CodeChanged(val value: String) : VerifyUiEvent
    data object Verify : VerifyUiEvent
    data object Resend : VerifyUiEvent
    data object Logout : VerifyUiEvent
}

sealed interface VerifyUiEffect {
    data class ShowMessage(val text: UiText) : VerifyUiEffect
}

@KoinViewModel
class VerifyEmailViewModel(
    private val auth: AuthUseCase,
    private val session: SessionUseCase,
    private val registrar: PushRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyUiState(email = session.user.value?.email.orEmpty()))
    val state: StateFlow<VerifyUiState> = _state.asStateFlow()

    private val _effect = Channel<VerifyUiEffect>(Channel.BUFFERED)
    val effect: Flow<VerifyUiEffect> = _effect.receiveAsFlow()

    fun onEvent(event: VerifyUiEvent) {
        when (event) {
            is VerifyUiEvent.CodeChanged -> _state.update { it.copy(code = event.value, error = null) }
            VerifyUiEvent.Verify -> verify()
            VerifyUiEvent.Resend -> resend()
            VerifyUiEvent.Logout -> logout()
        }
    }

    private fun verify() {
        val code = _state.value.code.trim()
        if (code.isBlank()) return
        run(R.string.verify_err) {
            val user = auth.verifyEmail(code)
            session.updateUser(user)
            _effect.trySend(VerifyUiEffect.ShowMessage(UiText.Res(R.string.verify_done)))
        }
    }

    private fun resend() {
        val email = _state.value.email
        if (email.isBlank()) return
        run(R.string.verify_err) {
            _effect.trySend(VerifyUiEffect.ShowMessage(UiText.Raw(auth.resendVerification(email))))
        }
    }

    private fun logout() {
        viewModelScope.launch {
            registrar.unregisterCurrent()
            runCatching { auth.logout() }
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
