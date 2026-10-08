package id.andreasmlbngaol.mpus.map.data.repository

import id.andreasmlbngaol.mpus.core.data.mapper.toDomain
import id.andreasmlbngaol.mpus.core.domain.model.CatMarker
import id.andreasmlbngaol.mpus.map.data.source.MapRemoteSource
import id.andreasmlbngaol.mpus.map.domain.repository.MapRepository

class MapRepositoryImpl(private val source: MapRemoteSource) : MapRepository {
    override suspend fun cats(minLat: Double, minLng: Double, maxLat: Double, maxLng: Double): List<CatMarker> =
        source.cats(minLat, minLng, maxLat, maxLng).map { it.toDomain() }
}
