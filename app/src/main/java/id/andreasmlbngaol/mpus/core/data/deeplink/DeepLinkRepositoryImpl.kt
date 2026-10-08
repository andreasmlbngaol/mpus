package id.andreasmlbngaol.mpus.core.data.deeplink

import id.andreasmlbngaol.mpus.core.domain.model.DeepLinkTarget
import id.andreasmlbngaol.mpus.core.domain.repository.DeepLinkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide, process-lifetime holder. A single instance must be shared by the FCM service,
 * the Activity and the nav layer, so it's registered as a Koin single (never per-feature).
 */
class DeepLinkRepositoryImpl : DeepLinkRepository {
    private val _pending = MutableStateFlow<DeepLinkTarget?>(null)
    override val pending: StateFlow<DeepLinkTarget?> = _pending.asStateFlow()

    // A counter so two identical taps still register as a new value (StateFlow drops equal
    // re-emits), rather than silently doing nothing the second time.
    private var seq = 0L

    override fun post(kind: String?, catId: String?) {
        _pending.value = DeepLinkTarget(kind, catId, ++seq)
    }

    override fun consume() {
        _pending.value = null
    }
}
