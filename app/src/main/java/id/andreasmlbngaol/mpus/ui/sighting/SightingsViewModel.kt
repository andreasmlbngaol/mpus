package id.andreasmlbngaol.mpus.ui.sighting

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.andreasmlbngaol.mpus.R
import id.andreasmlbngaol.mpus.data.ApiClient
import id.andreasmlbngaol.mpus.data.SightingResult
import id.andreasmlbngaol.mpus.ui.UiText
import id.andreasmlbngaol.mpus.ui.toUiText
import id.andreasmlbngaol.mpus.util.LocationProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

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

@KoinViewModel
class SightingsViewModel(
    private val api: ApiClient,
    private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SightingUiState())
    val state: StateFlow<SightingUiState> = _state.asStateFlow()

    private val _toast = Channel<UiText>(Channel.BUFFERED)
    val toast = _toast.receiveAsFlow()

    fun onPhotoCaptured(uri: Uri) {
        _state.update { it.copy(photoUri = uri, error = null, result = null, resolvedCatId = null) }
        locate()
    }

    /** Camera permission was refused; surface a hint without blocking the screen. */
    fun onCameraDenied() {
        _toast.trySend(UiText.Res(R.string.snap_err_no_camera))
    }

    fun locate() {
        viewModelScope.launch {
            _state.update { it.copy(locating = true) }
            val fix = LocationProvider.current(context)
            _state.update { it.copy(locating = false, lat = fix?.first, lng = fix?.second) }
            if (fix == null) _toast.trySend(UiText.Res(R.string.snap_err_no_fix))
        }
    }

    fun upload() {
        val s = _state.value
        val uri = s.photoUri
        if (uri == null) {
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
                val resolver = context.contentResolver
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Couldn't read that photo.")
                val result = api.createSighting(bytes, "cat.jpg", lat, lng, null)
                _state.update { it.copy(busy = false, result = result) }
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.toUiText(R.string.snap_err_upload)) }
            }
        }
    }

    fun linkTo(catId: String) = resolve(catId = catId, name = null)
    fun nameNew(name: String) = resolve(catId = null, name = name)

    private fun resolve(catId: String?, name: String?) {
        val sighting = _state.value.result?.sighting ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            try {
                val data = api.resolveSighting(sighting.id, catId, name)
                _state.update { it.copy(busy = false, resolvedCatId = data.catId) }
                _toast.trySend(UiText.Res(R.string.snap_linked))
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = e.toUiText(R.string.snap_err_resolve)) }
            }
        }
    }

    fun reset() = _state.update { SightingUiState() }
}
