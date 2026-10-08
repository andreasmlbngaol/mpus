package id.andreasmlbngaol.mpus.map.ui

import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.core.domain.model.GeoPoint
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.map.domain.model.Bounds

sealed interface MapUiEvent {
    /** One-shot at screen start: kicks off the auto-refresh loop. */
    data object Started : MapUiEvent
    data class ViewportChanged(val bounds: Bounds, val zoom: Double) : MapUiEvent
    data object Refresh : MapUiEvent
    data class Select(val marker: CatMarker?) : MapUiEvent
    data object Recenter : MapUiEvent
}

sealed interface MapUiEffect {
    /** Ask the map to animate to a fix. */
    data class CenterOn(val point: GeoPoint) : MapUiEffect
    data class ShowMessage(val text: UiText) : MapUiEffect
}
