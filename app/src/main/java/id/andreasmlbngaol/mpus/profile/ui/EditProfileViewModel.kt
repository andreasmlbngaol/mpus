package id.andreasmlbngaol.mpus.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.domain.usecase.SessionUseCase
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.core.ui.toUiText
import id.andreasmlbngaol.mpus.profile.domain.usecase.ProfileUseCase
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
class EditProfileViewModel(
    private val profile: ProfileUseCase,
    private val session: SessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    private val _effect = Channel<EditProfileUiEffect>(Channel.BUFFERED)
    val effect: Flow<EditProfileUiEffect> = _effect.receiveAsFlow()

    init {
        // Prefill from the cached user; the editor is a screen of its own now, so it
        // seeds itself instead of being handed state by a parent.
        val u = session.user.value
        if (u != null) _state.update { it.copy(username = u.username, nickname = u.nickname) }
    }

    fun onEvent(event: EditProfileUiEvent) {
        when (event) {
            is EditProfileUiEvent.UsernameChanged -> _state.update { it.copy(username = event.value) }
            is EditProfileUiEvent.NicknameChanged -> _state.update { it.copy(nickname = event.value) }
            EditProfileUiEvent.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                session.updateUser(profile.updateProfile(s.username.trim(), s.nickname.trim()))
                _state.update { it.copy(busy = false, saved = true) }
                _effect.trySend(EditProfileUiEffect.ShowMessage(UiText.Res(R.string.profile_updated)))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _effect.trySend(EditProfileUiEffect.ShowMessage(e.toUiText(R.string.profile_err_save)))
            }
        }
    }
}
