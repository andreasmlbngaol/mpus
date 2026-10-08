package id.andreasmlbngaol.mpus.profile.ui

import androidx.compose.runtime.Immutable
import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.profile.domain.model.UserProfile

@Immutable
data class UserUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val cats: List<CatMarker> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    val error: UiText? = null,
)
