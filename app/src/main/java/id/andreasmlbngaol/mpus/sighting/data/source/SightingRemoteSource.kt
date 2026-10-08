package id.andreasmlbngaol.mpus.sighting.data.source

import id.andreasmlbngaol.mpus.core.data.network.apiCall
import id.andreasmlbngaol.mpus.sighting.data.dto.ResolveDataDto
import id.andreasmlbngaol.mpus.sighting.data.dto.SightingResultDto
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ResolveBody(
    @kotlinx.serialization.SerialName("cat_id") val catId: String? = null,
    val name: String? = null,
)

class SightingRemoteSource(private val client: HttpClient, private val json: Json) {

    suspend fun create(
        bytes: ByteArray,
        filename: String,
        lat: Double,
        lng: Double,
        takenAt: String?,
    ): SightingResultDto = apiCall(json) {
        client.post("/sightings") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("photo", bytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/*")
                            append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                        })
                        append("lat", lat.toString())
                        append("lng", lng.toString())
                        takenAt?.let { append("taken_at", it) }
                    },
                ),
            )
        }
    }

    suspend fun resolve(sightingId: String, catId: String?, name: String?): ResolveDataDto =
        apiCall(json) {
            client.post("/sightings/$sightingId/resolve") {
                contentType(ContentType.Application.Json)
                setBody(ResolveBody(catId, name))
            }
        }
}
