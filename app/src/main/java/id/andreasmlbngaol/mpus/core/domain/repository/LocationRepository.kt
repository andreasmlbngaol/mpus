package id.andreasmlbngaol.mpus.core.domain.repository

import id.andreasmlbngaol.mpus.core.domain.model.GeoPoint

/** Best-effort device location, abstracted so ViewModels never touch Android APIs. */
interface LocationRepository {
    fun hasPermission(): Boolean
    suspend fun current(): GeoPoint?
}
