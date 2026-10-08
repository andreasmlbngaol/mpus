package id.andreasmlbngaol.mpus.profile.data.source

import id.andreasmlbngaol.mpus.core.data.dto.UserDto
import id.andreasmlbngaol.mpus.core.data.network.apiCall
import id.andreasmlbngaol.mpus.core.data.network.apiCallMessage
import id.andreasmlbngaol.mpus.profile.data.dto.MySightingsDto
import id.andreasmlbngaol.mpus.profile.data.dto.PatchMeBody
import id.andreasmlbngaol.mpus.profile.data.dto.UnreadCountDto
import id.andreasmlbngaol.mpus.profile.data.dto.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class ProfileRemoteSource(private val client: HttpClient, private val json: Json) {

    suspend fun mySightings(cursor: String?): MySightingsDto =
        apiCall(json) {
            client.get("/me/sightings") { cursor?.let { parameter("cursor", it) } }
        }

    suspend fun unreadCount(): UnreadCountDto = apiCall(json) { client.get("/notifications/unread-count") }

    suspend fun uploadAvatar(bytes: ByteArray, filename: String): UserDto =
        apiCall(json) {
            client.post("/me/avatar") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("avatar", bytes, Headers.build {
                                append(HttpHeaders.ContentType, "image/*")
                                append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                            })
                        },
                    ),
                )
            }
        }

    suspend fun deleteAvatar(): UserDto = apiCall(json) { client.delete("/me/avatar") }

    suspend fun updateProfile(username: String?, nickname: String?): UserDto =
        apiCall(json) {
            client.patch("/me") {
                contentType(ContentType.Application.Json)
                setBody(PatchMeBody(username, nickname))
            }
        }

    suspend fun logout() {
        apiCallMessage(json) { client.post("/auth/logout") }
    }

    suspend fun userProfile(id: String, cursor: String?): UserProfileDto =
        apiCall(json) {
            client.get("/users/$id") { cursor?.let { parameter("cursor", it) } }
        }
}
