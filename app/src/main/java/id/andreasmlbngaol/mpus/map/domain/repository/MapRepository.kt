package id.andreasmlbngaol.mpus.map.domain.repository

import id.andreasmlbngaol.mpus.core.domain.model.CatMarker

/** Cats within a viewport. */
interface MapRepository {
    suspend fun cats(minLat: Double, minLng: Double, maxLat: Double, maxLng: Double): List<CatMarker>
}
