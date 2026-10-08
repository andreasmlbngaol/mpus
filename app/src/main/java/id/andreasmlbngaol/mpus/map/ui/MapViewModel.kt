package id.andreasmlbngaol.mpus.map.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.domain.repository.LocationRepository
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.core.ui.toUiText
import id.andreasmlbngaol.mpus.map.domain.model.Bounds
import id.andreasmlbngaol.mpus.map.domain.usecase.MapUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MapViewModel(
    private val map: MapUseCase,
    private val location: LocationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private val _effect = Channel<MapUiEffect>(Channel.BUFFERED)
    val effect: Flow<MapUiEffect> = _effect.receiveAsFlow()

    private var last: Bounds? = null
    private var lastZoom: Double = 0.0
    private var debounce: Job? = null
    private var auto: Job? = null

    init {
        // Load the "you are here" fix once; the screen centers on it when it appears.
        viewModelScope.launch {
            location.current()?.let { fix -> _state.update { it.copy(myLocation = fix) } }
        }
    }

    fun onEvent(event: MapUiEvent) {
        when (event) {
            MapUiEvent.Started -> startAutoRefresh()
            is MapUiEvent.ViewportChanged -> onViewport(event.bounds, event.zoom)
            MapUiEvent.Refresh -> refresh()
            is MapUiEvent.Select -> _state.update { it.copy(selected = event.marker) }
            MapUiEvent.Recenter -> recenter()
        }
    }

    /**
     * Auto-refresh the visible cats on a slow loop so the map stays current without the
     * user hammering the button. Stops with the ViewModel.
     */
    private fun startAutoRefresh() {
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
    private fun onViewport(b: Bounds, zoom: Double) {
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

    private fun refresh() {
        if (lastZoom >= MinZoom) last?.let { load(it) }
    }

    private fun recenter() {
        viewModelScope.launch {
            val fix = _state.value.myLocation ?: location.current()?.also {
                _state.update { s -> s.copy(myLocation = it) }
            }
            if (fix != null) _effect.trySend(MapUiEffect.CenterOn(fix))
            else _effect.trySend(MapUiEffect.ShowMessage(UiText.Res(R.string.map_no_location)))
        }
    }

    private fun load(b: Bounds) {
        viewModelScope.launch {
            // Only the very first load owns the full-screen spinner; later loads (pan,
            // auto-refresh) update in place, otherwise the indicator blinks on and off
            // every time the viewport settles.
            val first = !_state.value.loadedOnce
            _state.update { it.copy(loading = first, error = null) }
            try {
                val markers = map.loadCats(b)
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
