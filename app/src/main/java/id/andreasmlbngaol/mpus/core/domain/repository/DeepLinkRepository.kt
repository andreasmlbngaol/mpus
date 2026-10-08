package id.andreasmlbngaol.mpus.core.domain.repository

import id.andreasmlbngaol.mpus.core.domain.model.DeepLinkTarget
import kotlinx.coroutines.flow.StateFlow

/**
 * Where a notification tap parks its target until the nav layer can pick it up. The FCM
 * service and the Activity can't touch the NavController, so they post here instead.
 */
interface DeepLinkRepository {
    val pending: StateFlow<DeepLinkTarget?>
    fun post(kind: String?, catId: String?)
    fun consume()
}
