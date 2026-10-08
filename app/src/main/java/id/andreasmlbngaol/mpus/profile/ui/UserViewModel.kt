package id.andreasmlbngaol.mpus.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.ui.toUiText
import id.andreasmlbngaol.mpus.profile.domain.usecase.UserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class UserViewModel(
    private val user: UserUseCase,
    @InjectedParam private val userId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(UserUiState())
    val state: StateFlow<UserUiState> = _state.asStateFlow()

    init { load() }

    fun onEvent(event: UserUiEvent) {
        when (event) {
            UserUiEvent.Refresh -> load()
            UserUiEvent.LoadMore -> loadMore()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val profile = user.profile(userId)
                _state.update {
                    it.copy(loading = false, profile = profile, cats = profile.cats.items, nextCursor = profile.cats.nextCursor)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText(R.string.user_err_load)) }
            }
        }
    }

    private fun loadMore() {
        val cursor = _state.value.nextCursor ?: return
        if (_state.value.loadingMore) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val page = user.profile(userId, cursor).cats
                _state.update {
                    it.copy(loadingMore = false, cats = it.cats + page.items, nextCursor = page.nextCursor)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }
}
