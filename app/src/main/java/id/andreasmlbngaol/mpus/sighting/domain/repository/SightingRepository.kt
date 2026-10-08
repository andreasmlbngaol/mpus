package id.andreasmlbngaol.mpus.sighting.domain.repository

import id.andreasmlbngaol.mpus.sighting.domain.model.SightingResult

/** Creating a sighting and telling the backend which cat it belongs to. */
interface SightingRepository {
    /** Upload a photo with its location; the backend returns candidates. */
    suspend fun create(bytes: ByteArray, filename: String, lat: Double, lng: Double, takenAt: String?): SightingResult

    /** Link the sighting to an existing cat ([catId]) or start a new one named [name]. */
    suspend fun resolve(sightingId: String, catId: String?, name: String?): String
}
