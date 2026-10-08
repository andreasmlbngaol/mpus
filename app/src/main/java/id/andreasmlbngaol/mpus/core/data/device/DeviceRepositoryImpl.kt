package id.andreasmlbngaol.mpus.core.data.device

import id.andreasmlbngaol.mpus.core.data.network.apiCallMessage
import id.andreasmlbngaol.mpus.core.domain.repository.DeviceRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeviceRemoteSource(private val client: HttpClient, private val json: Json) {

    @Serializable
    private data class TokenBody(val token: String, val platform: String? = null)

    suspend fun register(token: String, platform: String) {
        apiCallMessage(json) {
            client.post("/devices") {
                contentType(ContentType.Application.Json)
                setBody(TokenBody(token, platform))
            }
        }
    }

    suspend fun unregister(token: String) {
        apiCallMessage(json) {
            client.post("/devices/delete") {
                contentType(ContentType.Application.Json)
                setBody(TokenBody(token))
            }
        }
    }
}

class DeviceRepositoryImpl(private val source: DeviceRemoteSource) : DeviceRepository {
    override suspend fun register(token: String, platform: String) = source.register(token, platform)
    override suspend fun unregister(token: String) = source.unregister(token)
}
