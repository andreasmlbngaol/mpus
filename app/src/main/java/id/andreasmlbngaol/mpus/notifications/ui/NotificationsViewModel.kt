package id.andreasmlbngaol.mpus.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.core.ui.toUiText
import id.andreasmlbngaol.mpus.notifications.domain.usecase.NotificationsUseCase
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
class NotificationsViewModel(private val notifications: NotificationsUseCase) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private val _effect = Channel<NotificationsUiEffect>(Channel.BUFFERED)
    val effect: Flow<NotificationsUiEffect> = _effect.receiveAsFlow()

    init { load() }

    fun onEvent(event: NotificationsUiEvent) {
        when (event) {
            NotificationsUiEvent.Refresh -> load()
            NotificationsUiEvent.LoadMore -> loadMore()
            is NotificationsUiEvent.Approve -> resolve(event.id, approve = true)
            is NotificationsUiEvent.Reject -> resolve(event.id, approve = false)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val page = notifications.notifications()
                val merges = runCatching { notifications.pendingMerges() }.getOrDefault(emptyList())
                _state.update {
                    it.copy(loading = false, items = page.items, nextCursor = page.nextCursor, merges = merges)
                }
                // Opening the inbox clears the badge.
                runCatching { notifications.markSeen() }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText(R.string.notif_err_load)) }
            }
        }
    }

    private fun loadMore() {
        val s = _state.value
        val cursor = s.nextCursor ?: return
        if (s.loadingMore) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val page = notifications.notifications(cursor)
                _state.update { it.copy(loadingMore = false, items = it.items + page.items, nextCursor = page.nextCursor) }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }

    private fun resolve(id: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                if (approve) notifications.approveMerge(id) else notifications.rejectMerge(id)
                _state.update { it.copy(merges = it.merges.filterNot { m -> m.id == id }) }
                val text = UiText.Res(if (approve) R.string.merge_approved else R.string.merge_rejected)
                _effect.trySend(NotificationsUiEffect.ShowMessage(text))
            } catch (e: Exception) {
                _effect.trySend(NotificationsUiEffect.ShowMessage(e.toUiText(R.string.merge_err)))
            }
        }
    }
}
