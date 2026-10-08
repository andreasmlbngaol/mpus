package id.andreasmlbngaol.mpus.cat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.cat.domain.usecase.CatUseCase
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
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class CatViewModel(
    private val cat: CatUseCase,
    @InjectedParam private val catId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(CatUiState())
    val state: StateFlow<CatUiState> = _state.asStateFlow()

    private val _effect = Channel<CatUiEffect>(Channel.BUFFERED)
    val effect: Flow<CatUiEffect> = _effect.receiveAsFlow()

    init { load() }

    fun onEvent(event: CatUiEvent) {
        when (event) {
            CatUiEvent.Refresh -> load()
            is CatUiEvent.NameSubmitted -> addName(event.name)
            is CatUiEvent.NameLiked -> toggleLike(event.nameId)
            is CatUiEvent.ReviewSubmitted -> addReview(event.body, event.rating)
            is CatUiEvent.ReviewLiked -> toggleReviewLike(event.reviewId)
            is CatUiEvent.Report -> report(event.kind, event.targetId)
            CatUiEvent.LoadMergeTargets -> loadMergeTargets()
            is CatUiEvent.MergeInto -> mergeInto(event.targetId)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                _state.update { it.copy(loading = false, detail = cat.detail(catId)) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText(R.string.cat_err_load)) }
            }
        }
    }

    private fun addName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val detail = cat.setName(catId, name.trim())
                _state.update { it.copy(busy = false, detail = detail) }
                _effect.trySend(CatUiEffect.ShowMessage(UiText.Res(R.string.cat_name_saved)))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _effect.trySend(CatUiEffect.ShowMessage(e.toUiText(R.string.cat_err_save_name)))
            }
        }
    }

    private fun toggleLike(nameId: String) {
        viewModelScope.launch {
            try {
                cat.likeName(nameId)
                _state.update { it.copy(detail = cat.detail(catId)) }
            } catch (e: Exception) {
                _effect.trySend(CatUiEffect.ShowMessage(e.toUiText(R.string.cat_err_like)))
            }
        }
    }

    private fun addReview(body: String, rating: Int) {
        if (body.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val detail = cat.setReview(catId, body.trim(), rating)
                _state.update { it.copy(busy = false, detail = detail) }
                _effect.trySend(CatUiEffect.ShowMessage(UiText.Res(R.string.cat_review_saved)))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _effect.trySend(CatUiEffect.ShowMessage(e.toUiText(R.string.cat_err_review)))
            }
        }
    }

    private fun toggleReviewLike(reviewId: String) {
        viewModelScope.launch {
            try {
                cat.likeReview(reviewId)
                _state.update { it.copy(detail = cat.detail(catId)) }
            } catch (e: Exception) {
                _effect.trySend(CatUiEffect.ShowMessage(e.toUiText(R.string.cat_err_like)))
            }
        }
    }

    /** Flag a name or review. If enough people agree, it hides and the list refreshes. */
    private fun report(kind: String, targetId: String) {
        viewModelScope.launch {
            try {
                val hidden = cat.report(kind, targetId)
                if (hidden) {
                    _state.update { it.copy(detail = cat.detail(catId)) }
                    _effect.trySend(CatUiEffect.ShowMessage(UiText.Res(R.string.report_hidden)))
                } else {
                    _effect.trySend(CatUiEffect.ShowMessage(UiText.Res(R.string.report_sent)))
                }
            } catch (e: Exception) {
                _effect.trySend(CatUiEffect.ShowMessage(e.toUiText(R.string.report_err)))
            }
        }
    }

    /** Load the cats this viewer could merge into: their own sightings' cats. */
    private fun loadMergeTargets() {
        _state.update { it.copy(loadingTargets = true) }
        viewModelScope.launch {
            try {
                val targets = cat.mergeTargets(catId)
                _state.update { it.copy(loadingTargets = false, mergeTargets = targets) }
            } catch (e: Exception) {
                _state.update { it.copy(loadingTargets = false) }
            }
        }
    }

    /** Ask to merge this cat into [targetId]; the server decides if it needs the other owner. */
    private fun mergeInto(targetId: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val result = cat.requestMerge(catId, targetId)
                _state.update { it.copy(busy = false, mergeTargets = emptyList()) }
                _effect.trySend(
                    CatUiEffect.ShowMessage(
                        UiText.Res(
                            if (result.status == "merged") R.string.merge_approved else R.string.merge_request_sent,
                        ),
                    ),
                )
                if (result.status == "merged") _state.update { it.copy(detail = cat.detail(targetId)) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false) }
                _effect.trySend(CatUiEffect.ShowMessage(e.toUiText(R.string.merge_err)))
            }
        }
    }
}
