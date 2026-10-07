package id.andreasmlbngaol.mpus.ui.map

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.CatMarker
import id.andreasmlbngaol.mpus.ui.UiText
import id.andreasmlbngaol.mpus.ui.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@Immutable
data class MapUiState(
    val loading: Boolean = false,
    val loadedOnce: Boolean = false,
    val markers: List<CatMarker> = emptyList(),
    val error: UiText? = null,
    val selected: CatMarker? = null,
)

@Immutable
data class Bounds(val minLat: Double, val minLng: Double, val maxLat: Double, val maxLng: Double)

@KoinViewModel
class MapViewModel(private val api: ApiClient) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private var last: Bounds? = null
    private var lastZoom: Double = 0.0
    private var debounce: Job? = null
    private var auto: Job? = null

    /**
     * Auto-refresh the visible cats on a slow loop so the map stays current without the
     * user hammering the button. Stops with the ViewModel.
     */
    fun startAutoRefresh() {
        if (auto?.isActive == true) return
        auto = viewModelScope.launch {
            while (true) {
                delay(AutoRefreshMs)
                if (lastZoom >= MinZoom) last?.let { load(it) }
            }
        }
    }

    /**
     * Called on every camera move; debounced so panning doesn't hammer the API. Zoomed-out
     * viewports are ignored: at city scale the bbox would return hundreds of pins that are
     * unreadable anyway, so we wait until the user is zoomed in enough to matter.
     */
    fun onViewport(b: Bounds, zoom: Double) {
        lastZoom = zoom
        if (b.maxLat - b.minLat > 5 || b.maxLng - b.minLng > 5) return
        if (zoom < MinZoom) return
        last = b
        debounce?.cancel()
        debounce = viewModelScope.launch {
            delay(400)
            load(b)
        }
    }

    fun refresh() {
        if (lastZoom >= MinZoom) last?.let { load(it) }
    }

    fun select(marker: CatMarker?) = _state.update { it.copy(selected = marker) }

    private fun load(b: Bounds) {
        viewModelScope.launch {
            // Only the very first load owns the full-screen spinner; later loads (pan,
            // auto-refresh) update in place, otherwise the indicator blinks on and off
            // every time the viewport settles.
            val first = !_state.value.loadedOnce
            _state.update { it.copy(loading = first, error = null) }
            try {
                val markers = api.cats(b.minLat, b.minLng, b.maxLat, b.maxLng)
                _state.update { it.copy(loading = false, loadedOnce = true, markers = markers) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText(R.string.map_err_load)) }
            }
        }
    }

    private companion object {
        const val AutoRefreshMs = 30_000L

        /** Below this zoom the viewport is too wide to be useful; don't fetch. */
        const val MinZoom = 13.0
    }
}
