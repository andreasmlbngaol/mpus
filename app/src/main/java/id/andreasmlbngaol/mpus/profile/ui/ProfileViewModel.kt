package id.andreasmlbngaol.mpus.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.domain.repository.PushRepository
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
class ProfileViewModel(
    private val profile: ProfileUseCase,
    private val session: SessionUseCase,
    private val push: PushRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _effect = Channel<ProfileUiEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileUiEffect> = _effect.receiveAsFlow()

    init { load() }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.Refresh -> load()
            ProfileUiEvent.RefreshUnread -> refreshUnread()
            ProfileUiEvent.LoadMore -> loadMore()
            is ProfileUiEvent.UploadAvatar -> uploadAvatar(event.bytes)
            ProfileUiEvent.DeleteAvatar -> deleteAvatar()
            ProfileUiEvent.Logout -> logout()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val page = profile.mySightings()
                _state.update {
                    it.copy(
                        loading = false,
                        sightings = page.items,
                        nextCursor = page.nextCursor,
                        sightingsCount = page.sightingsCount,
                        catsCount = page.catsCount,
                        unnamedCount = page.unnamedCount,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText(R.string.profile_err_load)) }
            }
            refreshUnread()
        }
    }

    /** One-shot refresh of the bell badge. The screen polls this while it's visible. */
    private fun refreshUnread() {
        viewModelScope.launch {
            runCatching { profile.unreadCount() }.onSuccess { count ->
                _state.update { it.copy(unread = count) }
            }
        }
    }

    /** Append the next page when the grid nears its end. No-op on the last page. */
    private fun loadMore() {
        val s = _state.value
        val cursor = s.nextCursor ?: return
        if (s.loadingMore) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val page = profile.mySightings(cursor)
                _state.update {
                    it.copy(
                        loadingMore = false,
                        sightings = it.sightings + page.items,
                        nextCursor = page.nextCursor,
                    )
                }
            } catch (e: Exception) {
                // Keep the pages we already have; a failed "more" shouldn't wipe the grid.
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }

    private fun uploadAvatar(bytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                session.updateUser(profile.uploadAvatar(bytes, "avatar.webp"))
                _state.update { it.copy(busy = false) }
                _effect.trySend(ProfileUiEffect.ShowMessage(UiText.Res(R.string.profile_avatar_done)))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _effect.trySend(ProfileUiEffect.ShowMessage(e.toUiText(R.string.profile_err_avatar)))
            }
        }
    }

    private fun deleteAvatar() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                session.updateUser(profile.deleteAvatar())
                _state.update { it.copy(busy = false) }
                _effect.trySend(ProfileUiEffect.ShowMessage(UiText.Res(R.string.profile_avatar_removed)))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _effect.trySend(ProfileUiEffect.ShowMessage(e.toUiText(R.string.profile_err_avatar_delete)))
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            // Drop this device's push token before the session dies, then sign out.
            push.unregisterCurrent()
            runCatching { profile.logout() }
            session.clear()
        }
    }
}
