package id.andreasmlbngaol.mpus.cat.ui

import androidx.compose.runtime.Immutable
import id.andreasmlbngaol.mpus.cat.domain.model.CatDetail
import id.andreasmlbngaol.mpus.cat.domain.model.MergeTarget
import id.andreasmlbngaol.mpus.core.ui.UiText

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
