package id.andreasmlbngaol.mpus.map.data.source

import id.andreasmlbngaol.mpus.core.data.dto.CatMarkerDto
import id.andreasmlbngaol.mpus.core.data.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json

class MapRemoteSource(private val client: HttpClient, private val json: Json) {
    suspend fun cats(minLat: Double, minLng: Double, maxLat: Double, maxLng: Double): List<CatMarkerDto> =
        apiCall(json) {
            client.get("/cats") {
                parameter("min_lat", minLat)
                parameter("min_lng", minLng)
                parameter("max_lat", maxLat)
                parameter("max_lng", maxLng)
            }
        }
}
