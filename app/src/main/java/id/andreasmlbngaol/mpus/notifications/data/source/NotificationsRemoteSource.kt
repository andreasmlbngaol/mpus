package id.andreasmlbngaol.mpus.notifications.data.source

import id.andreasmlbngaol.mpus.core.data.dto.MergeRequestDto
import id.andreasmlbngaol.mpus.core.data.dto.PageDto
import id.andreasmlbngaol.mpus.core.data.network.apiCall
import id.andreasmlbngaol.mpus.core.data.network.apiCallMessage
import id.andreasmlbngaol.mpus.notifications.data.dto.AppNotificationDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.serialization.json.Json

class NotificationsRemoteSource(private val client: HttpClient, private val json: Json) {

    suspend fun notifications(cursor: String?): PageDto<AppNotificationDto> =
        apiCall(json) {
            client.get("/notifications") { cursor?.let { parameter("cursor", it) } }
        }

    suspend fun unreadCount(): Long = apiCall<UnreadCountDto>(json) { client.get("/notifications/unread-count") }.count

    suspend fun markSeen() = apiCallMessage(json) { client.post("/notifications/seen") }

    suspend fun pendingMerges(): List<MergeRequestDto> = apiCall(json) { client.get("/merges/pending") }

    suspend fun approveMerge(id: String): MergeRequestDto = apiCall(json) { client.post("/merges/$id/approve") }

    suspend fun rejectMerge(id: String) = apiCallMessage(json) { client.post("/merges/$id/reject") }
}

@kotlinx.serialization.Serializable
private data class UnreadCountDto(val count: Long = 0)
