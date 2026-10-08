package id.andreasmlbngaol.mpus.cat.data.dto

import id.andreasmlbngaol.mpus.core.data.dto.SightingDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatNameDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val nickname: String,
    val name: String,
    val likes: Int,
    @SerialName("liked_by_me") val likedByMe: Boolean,
    val mine: Boolean = false,
)

@Serializable
data class CatReviewDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val nickname: String,
    val body: String,
    val rating: Int,
    val likes: Int,
    @SerialName("liked_by_me") val likedByMe: Boolean,
    val mine: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class CatDetailDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("created_at") val createdAt: String,
    val names: List<CatNameDto> = emptyList(),
    val reviews: List<CatReviewDto> = emptyList(),
    val sightings: List<SightingDto> = emptyList(),
    @SerialName("can_contribute") val canContribute: Boolean = false,
)

@Serializable
data class LikeDataDto(val liked: Boolean)

/** Whether a report just pushed an item over the hide threshold. */
@Serializable
data class ReportResultDto(val hidden: Boolean = false)
