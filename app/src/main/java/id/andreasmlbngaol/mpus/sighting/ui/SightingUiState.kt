package id.andreasmlbngaol.mpus.sighting.ui

import androidx.compose.runtime.Immutable
import android.net.Uri
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.sighting.domain.model.SightingResult

@Immutable
data class SightingUiState(
    val photoUri: Uri? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val locating: Boolean = false,
    val busy: Boolean = false,
    val error: UiText? = null,
    val result: SightingResult? = null,
    val resolvedCatId: String? = null,
)
