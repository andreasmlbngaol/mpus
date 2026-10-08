package id.andreasmlbngaol.mpus.sighting.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.core.domain.repository.LocationRepository
import id.andreasmlbngaol.mpus.core.ui.UiText
import id.andreasmlbngaol.mpus.core.ui.toUiText
import id.andreasmlbngaol.mpus.sighting.domain.usecase.SightingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SightingsViewModel(
    private val sighting: SightingUseCase,
    private val location: LocationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SightingUiState())
    val state: StateFlow<SightingUiState> = _state.asStateFlow()

    private val _effect = Channel<SightingUiEffect>(Channel.BUFFERED)
    val effect: Flow<SightingUiEffect> = _effect.receiveAsFlow()

    fun onEvent(event: SightingUiEvent) {
        when (event) {
            is SightingUiEvent.PhotoCaptured -> {
                _state.update {
                    it.copy(photoUri = event.uri, error = null, result = null, resolvedCatId = null)
                }
                locate()
            }
            SightingUiEvent.CameraDenied -> _effect.trySend(SightingUiEffect.ShowMessage(UiText.Res(R.string.snap_err_no_camera)))
            SightingUiEvent.Locate -> locate()
            is SightingUiEvent.Upload -> upload(event.bytes)
            is SightingUiEvent.LinkTo -> resolve(catId = event.catId, name = null)
            is SightingUiEvent.NameNew -> resolve(catId = null, name = event.name)
            SightingUiEvent.Reset -> _state.update { SightingUiState() }
        }
    }

    private fun locate() {
        viewModelScope.launch {
            _state.update { it.copy(locating = true) }
            val fix = location.current()
            _state.update { it.copy(locating = false, lat = fix?.lat, lng = fix?.lng) }
            if (fix == null) _effect.trySend(SightingUiEffect.ShowMessage(UiText.Res(R.string.snap_err_no_fix)))
        }
    }

    private fun upload(bytes: ByteArray) {
        val s = _state.value
        if (s.photoUri == null) {
            _state.update { it.copy(error = UiText.Res(R.string.snap_err_no_photo)) }
            return
        }
        val lat = s.lat
        val lng = s.lng
        if (lat == null || lng == null) {
            _state.update { it.copy(error = UiText.Res(R.string.snap_err_no_location)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            try {
                val result = sighting.create(bytes, "cat.jpg", lat, lng, null)
                _state.update { it.copy(busy = false, result = result) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.toUiText(R.string.snap_err_upload)) }
            }
        }
    }

    private fun resolve(catId: String?, name: String?) {
        val sightingId = _state.value.result?.sighting?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            try {
                val resolved = sighting.resolve(sightingId, catId, name)
                _state.update { it.copy(busy = false, resolvedCatId = resolved) }
                _effect.trySend(SightingUiEffect.ShowMessage(UiText.Res(R.string.snap_linked)))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.toUiText(R.string.snap_err_resolve)) }
            }
        }
    }
}
