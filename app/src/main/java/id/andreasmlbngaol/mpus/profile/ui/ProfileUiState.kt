package id.andreasmlbngaol.mpus.profile.ui

import androidx.compose.runtime.Immutable
import id.andreasmlbngaol.mpus.core.domain.model.Sighting
import id.andreasmlbngaol.mpus.core.ui.UiText

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
