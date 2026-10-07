package id.andreasmlbngaol.mpus.ui.cat

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.CatDetail
import id.andreasmlbngaol.mpus.ui.UiText
import id.andreasmlbngaol.mpus.ui.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/** A cat the viewer could merge this one into: just enough to show a chip. */
@Immutable
data class MergeTarget(val id: String, val name: String?, val thumbUrl: String?)

@Immutable
data class CatUiState(
    val loading: Boolean = true,
    val detail: CatDetail? = null,
    val error: UiText? = null,
    val busy: Boolean = false,
    /** Cats you've also created, as merge targets. Loaded on demand. */
    val mergeTargets: List<MergeTarget> = emptyList(),
    val loadingTargets: Boolean = false,
)

@KoinViewModel
class CatViewModel(
    private val api: ApiClient,
    @InjectedParam private val catId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(CatUiState())
    val state: StateFlow<CatUiState> = _state.asStateFlow()

    private val _toast = Channel<UiText>(Channel.BUFFERED)
    val toast = _toast.receiveAsFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                _state.update { it.copy(loading = false, detail = api.catDetail(catId)) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText(R.string.cat_err_load)) }
            }
        }
    }

    fun addName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val detail = api.setName(catId, name.trim())
                _state.update { it.copy(busy = false, detail = detail) }
                _toast.trySend(UiText.Res(R.string.cat_name_saved))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _toast.trySend(e.toUiText(R.string.cat_err_save_name))
            }
        }
    }

    fun toggleLike(nameId: String) {
        viewModelScope.launch {
            try {
                api.like(nameId)
                _state.update { it.copy(detail = api.catDetail(catId)) }
            } catch (e: Exception) {
                _toast.trySend(e.toUiText(R.string.cat_err_like))
            }
        }
    }

    fun addReview(body: String, rating: Int) {
        if (body.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val detail = api.setReview(catId, body.trim(), rating)
                _state.update { it.copy(busy = false, detail = detail) }
                _toast.trySend(UiText.Res(R.string.cat_review_saved))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _toast.trySend(e.toUiText(R.string.cat_err_review))
            }
        }
    }

    fun toggleReviewLike(reviewId: String) {
        viewModelScope.launch {
            try {
                api.likeReview(reviewId)
                _state.update { it.copy(detail = api.catDetail(catId)) }
            } catch (e: Exception) {
                _toast.trySend(e.toUiText(R.string.cat_err_like))
            }
        }
    }

    /** Flag a name or review. If enough people agree, it hides and the list refreshes. */
    fun report(kind: String, targetId: String) {
        viewModelScope.launch {
            try {
                val result = api.report(kind, targetId)
                if (result.hidden) {
                    _state.update { it.copy(detail = api.catDetail(catId)) }
                    _toast.trySend(UiText.Res(R.string.report_hidden))
                } else {
                    _toast.trySend(UiText.Res(R.string.report_sent))
                }
            } catch (e: Exception) {
                _toast.trySend(e.toUiText(R.string.report_err))
            }
        }
    }

    /** Load the cats this viewer could merge into: their own sightings' cats. */
    fun loadMergeTargets() {
        _state.update { it.copy(loadingTargets = true) }
        viewModelScope.launch {
            try {
                val mine = api.mySightings().items.mapNotNull { it.catId }.distinct().filter { it != catId }
                val targets = mine.mapNotNull { id ->
                    runCatching { api.catDetail(id) }.getOrNull()?.let {
                        MergeTarget(it.id, it.displayName, it.sightings.firstOrNull()?.thumbUrl)
                    }
                }
                _state.update { it.copy(loadingTargets = false, mergeTargets = targets) }
            } catch (e: Exception) {
                _state.update { it.copy(loadingTargets = false) }
            }
        }
    }

    /** Ask to merge this cat into [targetId]; the server decides if it needs the other owner. */
    fun mergeInto(targetId: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val result = api.requestMerge(catId, targetId)
                _state.update { it.copy(busy = false, mergeTargets = emptyList()) }
                _toast.trySend(
                    UiText.Res(
                        if (result.status == "merged") R.string.merge_approved else R.string.merge_request_sent,
                    ),
                )
                if (result.status == "merged") _state.update { it.copy(detail = api.catDetail(targetId)) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _toast.trySend(e.toUiText(R.string.merge_err))
            }
        }
    }
}
