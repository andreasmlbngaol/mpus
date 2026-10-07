package id.andreasmlbngaol.mpus.ui.notifications

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.AppNotification
import id.andreasmlbngaol.mpus.data.MergeRequest
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
data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    val merges: List<MergeRequest> = emptyList(),
    val loading: Boolean = true,
    val error: UiText? = null,
)

@KoinViewModel
class NotificationsViewModel(private val api: ApiClient) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private val _toast = Channel<UiText>(Channel.BUFFERED)
    val toast = _toast.receiveAsFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val page = api.notifications()
                val merges = runCatching { api.pendingMerges() }.getOrDefault(emptyList())
                _state.update {
                    it.copy(loading = false, items = page.items, nextCursor = page.nextCursor, merges = merges)
                }
                // Opening the inbox clears the badge.
                runCatching { api.markNotificationsSeen() }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText(R.string.notif_err_load)) }
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        val cursor = s.nextCursor ?: return
        if (s.loadingMore) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val page = api.notifications(cursor)
                _state.update { it.copy(loadingMore = false, items = it.items + page.items, nextCursor = page.nextCursor) }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }

    fun approve(id: String) {
        viewModelScope.launch {
            try {
                api.approveMerge(id)
                _state.update { it.copy(merges = it.merges.filterNot { m -> m.id == id }) }
                _toast.trySend(UiText.Res(R.string.merge_approved))
            } catch (e: Exception) {
                _toast.trySend(e.toUiText(R.string.merge_err))
            }
        }
    }

    fun reject(id: String) {
        viewModelScope.launch {
            try {
                api.rejectMerge(id)
                _state.update { it.copy(merges = it.merges.filterNot { m -> m.id == id }) }
                _toast.trySend(UiText.Res(R.string.merge_rejected))
            } catch (e: Exception) {
                _toast.trySend(e.toUiText(R.string.merge_err))
            }
        }
    }
}
