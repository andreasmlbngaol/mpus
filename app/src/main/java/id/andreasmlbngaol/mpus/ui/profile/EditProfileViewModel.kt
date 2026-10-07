package id.andreasmlbngaol.mpus.ui.profile

import androidx.compose.runtime.Immutable
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

@Immutable
data class EditProfileUiState(
    val username: String = "",
    val nickname: String = "",
    val busy: Boolean = false,
    val saved: Boolean = false,
)

@KoinViewModel
class EditProfileViewModel(
    private val api: ApiClient,
    private val session: Session,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    private val _toast = Channel<UiText>(Channel.BUFFERED)
    val toast = _toast.receiveAsFlow()

    init {
        // Prefill from the cached user; the editor is a screen of its own now, so it
        // seeds itself instead of being handed state by a parent.
        val u = session.user.value
        if (u != null) _state.update { it.copy(username = u.username, nickname = u.nickname) }
    }

    fun onUsername(v: String) = _state.update { it.copy(username = v) }
    fun onNickname(v: String) = _state.update { it.copy(nickname = v) }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val user = api.patchMe(s.username.trim(), s.nickname.trim())
                session.updateUser(user)
                _state.update { it.copy(busy = false, saved = true) }
                _toast.trySend(UiText.Res(R.string.profile_updated))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _toast.trySend(e.toUiText(R.string.profile_err_save))
            }
        }
    }
}
