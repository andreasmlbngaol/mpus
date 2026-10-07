package id.andreasmlbngaol.mpus.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A notification tap that wants to open a screen. The FCM service (and the system, for a
 * background push) can't touch the NavController, so they drop the target here and the
 * nav layer picks it up — which also means a tap while signed out waits until sign-in.
 *
 * A plain `object`: no dependencies, reachable from the service, the Activity, and Compose.
 */
object DeepLinks {
    data class Target(val kind: String?, val catId: String?, val seq: Long)

    private val _pending = MutableStateFlow<Target?>(null)
    val pending: StateFlow<Target?> = _pending.asStateFlow()

    // A counter so two identical taps still register as a new value (StateFlow drops
    // equal re-emits), rather than silently doing nothing the second time.
    private var seq = 0L

    fun post(kind: String?, catId: String?) {
        _pending.value = Target(kind, catId, ++seq)
    }

    fun consume() {
        _pending.value = null
    }
}
