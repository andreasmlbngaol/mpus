package id.andreasmlbngaol.mpus.map.ui

import androidx.compose.runtime.Immutable
import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.core.domain.model.GeoPoint
import id.andreasmlbngaol.mpus.core.ui.UiText

@Immutable
data class MapUiState(
    val loading: Boolean = false,
    val loadedOnce: Boolean = false,
    val markers: List<CatMarker> = emptyList(),
    val error: UiText? = null,
    val selected: CatMarker? = null,
    /** The user's own fix, once known. */
    val myLocation: GeoPoint? = null,
)
