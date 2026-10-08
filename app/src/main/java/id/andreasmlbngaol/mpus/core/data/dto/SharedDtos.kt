package id.andreasmlbngaol.mpus.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatMarkerDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("thumb_url") val thumbUrl: String,
    val lat: Double,
    val lng: Double,
    @SerialName("sighting_count") val sightingCount: Long,
)

@Serializable
data class SightingDto(
    val id: String,
    @SerialName("cat_id") val catId: String? = null,
    @SerialName("photo_url") val photoUrl: String,
    @SerialName("thumb_url") val thumbUrl: String,
    val lat: Double,
    val lng: Double,
    @SerialName("taken_at") val takenAt: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class PageDto<T>(
    val items: List<T> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)

@Serializable
data class MergeRequestDto(
    val id: String,
    @SerialName("source_cat_id") val sourceCatId: String,
    @SerialName("target_cat_id") val targetCatId: String,
    @SerialName("source_name") val sourceName: String? = null,
    @SerialName("target_name") val targetName: String? = null,
    @SerialName("requested_by") val requestedBy: String,
    val status: String,
)
