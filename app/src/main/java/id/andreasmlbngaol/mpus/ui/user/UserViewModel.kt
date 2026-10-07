package id.andreasmlbngaol.mpus.ui.user

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.CatMarker
import id.andreasmlbngaol.mpus.data.UserProfile
import id.andreasmlbngaol.mpus.ui.UiText
import id.andreasmlbngaol.mpus.ui.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@Immutable
data class UserUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val cats: List<CatMarker> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    val error: UiText? = null,
)

@KoinViewModel
class UserViewModel(
    private val api: ApiClient,
    @InjectedParam private val userId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(UserUiState())
    val state: StateFlow<UserUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val profile = api.userProfile(userId)
                _state.update {
                    it.copy(loading = false, profile = profile, cats = profile.cats.items, nextCursor = profile.cats.nextCursor)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText(R.string.user_err_load)) }
            }
        }
    }

    fun loadMore() {
        val cursor = _state.value.nextCursor ?: return
        if (_state.value.loadingMore) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val page = api.userProfile(userId, cursor).cats
                _state.update {
                    it.copy(loadingMore = false, cats = it.cats + page.items, nextCursor = page.nextCursor)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }
}
