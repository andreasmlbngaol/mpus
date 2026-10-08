package id.andreasmlbngaol.mpus.sighting.domain.usecase

import id.andreasmlbngaol.mpus.sighting.domain.model.SightingResult
import id.andreasmlbngaol.mpus.sighting.domain.repository.SightingRepository

/** Creating a sighting and resolving it to a cat. */
class SightingUseCase(private val repo: SightingRepository) {
    suspend fun create(
        bytes: ByteArray,
        filename: String,
        lat: Double,
        lng: Double,
        takenAt: String?,
    ): SightingResult = repo.create(bytes, filename, lat, lng, takenAt)

    /** Returns the id of the cat the sighting was linked to. */
    suspend fun resolve(sightingId: String, catId: String?, name: String?): String =
        repo.resolve(sightingId, catId, name)
}
