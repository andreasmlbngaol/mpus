package id.andreasmlbngaol.mpus.sighting.data.repository

import id.andreasmlbngaol.mpus.sighting.data.mapper.toDomain
import id.andreasmlbngaol.mpus.sighting.data.source.SightingRemoteSource
import id.andreasmlbngaol.mpus.sighting.domain.model.SightingResult
import id.andreasmlbngaol.mpus.sighting.domain.repository.SightingRepository

class SightingRepositoryImpl(private val source: SightingRemoteSource) : SightingRepository {

    override suspend fun create(
        bytes: ByteArray,
        filename: String,
        lat: Double,
        lng: Double,
        takenAt: String?,
    ): SightingResult = source.create(bytes, filename, lat, lng, takenAt).toDomain()

    override suspend fun resolve(sightingId: String, catId: String?, name: String?): String =
        source.resolve(sightingId, catId, name).catId
}
