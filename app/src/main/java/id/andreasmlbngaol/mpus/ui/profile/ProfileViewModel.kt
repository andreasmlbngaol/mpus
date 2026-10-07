package id.andreasmlbngaol.mpus.ui.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.Session
import id.andreasmlbngaol.mpus.data.Sighting
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
data class ProfileUiState(
    val sightings: List<Sighting> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    val sightingsCount: Long = 0,
    val catsCount: Long = 0,
    val unnamedCount: Long = 0,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: UiText? = null,
    val unread: Long = 0,
)

@KoinViewModel
class ProfileViewModel(
    private val api: ApiClient,
    private val session: Session,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _toast = Channel<UiText>(Channel.BUFFERED)
    val toast = _toast.receiveAsFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val page = api.mySightings()
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
    fun refreshUnread() {
        viewModelScope.launch {
            runCatching { api.unreadCount() }.onSuccess { count ->
                _state.update { it.copy(unread = count.count) }
            }
        }
    }

    /** Append the next page when the grid nears its end. No-op on the last page. */
    fun loadMore() {
        val s = _state.value
        val cursor = s.nextCursor ?: return
        if (s.loadingMore) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val page = api.mySightings(cursor)
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

    fun uploadAvatar(bytes: ByteArray, filename: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val user = api.uploadAvatar(bytes, filename)
                session.updateUser(user)
                _state.update { it.copy(busy = false) }
                _toast.trySend(UiText.Res(R.string.profile_avatar_done))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _toast.trySend(e.toUiText(R.string.profile_err_avatar))
            }
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                session.updateUser(api.deleteAvatar())
                _state.update { it.copy(busy = false) }
                _toast.trySend(UiText.Res(R.string.profile_avatar_removed))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _toast.trySend(e.toUiText(R.string.profile_err_avatar_delete))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { api.logout() }
            session.clear()
        }
    }
}
