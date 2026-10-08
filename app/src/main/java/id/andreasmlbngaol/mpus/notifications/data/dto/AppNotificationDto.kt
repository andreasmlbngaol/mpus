package id.andreasmlbngaol.mpus.notifications.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppNotificationDto(
    val id: String,
    val kind: String,
    @SerialName("actor_nickname") val actorNickname: String? = null,
    @SerialName("actor_avatar_url") val actorAvatarUrl: String? = null,
    @SerialName("cat_id") val catId: String? = null,
    @SerialName("cat_name") val catName: String? = null,
    val seen: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)
