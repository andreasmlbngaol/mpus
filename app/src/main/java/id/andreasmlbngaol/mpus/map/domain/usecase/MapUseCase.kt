package id.andreasmlbngaol.mpus.map.domain.usecase

import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.map.domain.model.Bounds
import id.andreasmlbngaol.mpus.map.domain.repository.MapRepository

/** Loads the cats inside a viewport. */
class MapUseCase(private val repo: MapRepository) {
    suspend fun loadCats(bounds: Bounds): List<CatMarker> =
        repo.cats(bounds.minLat, bounds.minLng, bounds.maxLat, bounds.maxLng)
}
